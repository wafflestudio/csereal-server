package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.mockauth.CustomOidcUser
import com.wafflestudio.csereal.core.reservation.database.ReservationRepository
import com.wafflestudio.csereal.core.reservation.database.RoomEntity
import com.wafflestudio.csereal.core.reservation.database.RoomRepository
import com.wafflestudio.csereal.core.reservation.database.RoomType
import com.wafflestudio.csereal.core.reservation.dto.ReserveRequest
import com.wafflestudio.csereal.core.user.database.UserEntity
import com.wafflestudio.csereal.core.user.database.UserRepository
import com.wafflestudio.csereal.global.config.MySQLTestContainerConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import org.springframework.aop.support.AopUtils
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import java.time.LocalDateTime
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@ActiveProfiles("test")
@SpringBootTest
@Import(MySQLTestContainerConfig::class)
class ReservationConcurrencyIntegrationTest(
    private val roomRepository: RoomRepository,
    private val reservationRepository: ReservationRepository,
    private val reservationService: ReservationService,
    private val userRepository: UserRepository
) : FunSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    lateinit var room: RoomEntity
    lateinit var staff: UserEntity

    beforeSpec {
        room = roomRepository.saveAndFlush(
            RoomEntity("concurrency test room", "concurrency", 20, RoomType.SEMINAR)
        )
        staff = userRepository.findByUsername("test") ?: userRepository.saveAndFlush(
            UserEntity("test", "test", "test@abc.com", "0000-00000")
        )
    }

    beforeTest {
        reservationRepository.deleteAll()
        reservationRepository.flush()
    }

    afterSpec {
        reservationRepository.deleteAll()
        roomRepository.deleteById(room.id)
    }

    test("partial overlap is serialized by the room lock in independent transactions") {
        AopUtils.isAopProxy(reservationService) shouldBe true
        val start = LocalDateTime.now().plusDays(1)
        val requests = listOf(
            request(room.id, "first", start, start.plusHours(1)),
            request(
                room.id,
                "partial overlap",
                start.plusMinutes(30),
                start.plusHours(1).plusMinutes(30)
            )
        )
        val latch = CountDownLatch(requests.size)
        val executor = Executors.newFixedThreadPool(requests.size)
        val results = Collections.synchronizedList(mutableListOf<Result<Unit>>())

        requests.forEach { request ->
            executor.submit {
                try {
                    authenticateStaff(staff)
                    latch.countDown()
                    latch.await()
                    reservationService.reserveRoom(request)
                    results.add(Result.success(Unit))
                } catch (exception: Exception) {
                    results.add(Result.failure(exception))
                } finally {
                    SecurityContextHolder.clearContext()
                }
            }
        }
        executor.shutdown()
        executor.awaitTermination(10, TimeUnit.SECONDS) shouldBe true

        results.count { it.isSuccess } shouldBe 1
        results.count { it.isFailure } shouldBe 1
        results.single { it.isFailure }.exceptionOrNull() shouldBe
            CserealException(ErrorCode.RESERVATION_OCCUPIED)
        reservationRepository.findByRoomIdAndTimeOverlap(
            room.id,
            start,
            start.plusHours(1).plusMinutes(30)
        ).size shouldBe 1
    }
}) {
    companion object {
        private fun authenticateStaff(user: UserEntity) {
            val authorities = listOf(SimpleGrantedAuthority("ROLE_STAFF"))
            val issuedAt = Instant.now()
            val principal = CustomOidcUser(
                user,
                authorities,
                OidcIdToken("mock-token", issuedAt, issuedAt.plusSeconds(3600), mapOf("sub" to user.username))
            )
            SecurityContextHolder.getContext().authentication =
                UsernamePasswordAuthenticationToken(principal, null, authorities)
        }

        private fun request(
            roomId: Long,
            title: String,
            start: LocalDateTime,
            end: LocalDateTime
        ) = ReserveRequest(
            roomId = roomId,
            title = title,
            contactEmail = "a@a.com",
            contactPhone = "010-1234-5678",
            professor = "prof",
            purpose = "purpose",
            startTime = start,
            endTime = end,
            agreed = true
        )
    }
}
