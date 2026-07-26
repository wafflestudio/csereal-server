package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.mockauth.CustomOidcUser
import com.wafflestudio.csereal.core.reservation.database.ReservationType
import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermRepository
import com.wafflestudio.csereal.core.reservation.database.RoomEntity
import com.wafflestudio.csereal.core.reservation.database.RoomRepository
import com.wafflestudio.csereal.core.reservation.database.RoomType
import com.wafflestudio.csereal.core.reservation.dto.ReserveRequest
import com.wafflestudio.csereal.core.user.database.UserEntity
import com.wafflestudio.csereal.core.user.database.UserRepository
import com.wafflestudio.csereal.global.config.MySQLTestContainerConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@Import(MySQLTestContainerConfig::class)
class ReserveTermServiceTest(
    private val roomRepository: RoomRepository,
    private val reservationService: ReservationService,
    private val reserveTermPolicy: ReserveTermPolicy,
    private val reserveTermRepository: ReserveTermRepository,
    private val userRepository: UserRepository
) : StringSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))
    lateinit var room: RoomEntity

    beforeSpec {
        val generatedRoom = roomRepository.save(RoomEntity("labmaster room", "303", 20, RoomType.SEMINAR))
        room = if (generatedRoom.id == 8L) {
            roomRepository.save(RoomEntity("labmaster room", "303", 20, RoomType.SEMINAR))
        } else {
            generatedRoom
        }
    }
    beforeTest {
        val now = reserveTermPolicy.now()
        reserveTermRepository.deleteAll()
        reserveTermRepository.save(
            ReserveTermEntity(now.minusDays(2), now.minusDays(1), now.minusDays(3), now.plusYears(1))
        )
        authenticateLabmaster(userRepository)
    }
    afterTest { SecurityContextHolder.clearContext() }

    "at or after the target term start a labmaster gets a ONE_TIME reservation" {
        val start = reserveTermPolicy.now().toLocalDate().plusDays(1).atTime(10, 0)
        reservationService.reserveRoom(request(room.id, start, 1))
            .single().reservationType shouldBe ReservationType.ONE_TIME
    }

    "at or after target term start a labmaster cannot recur" {
        val start = reserveTermPolicy.now().toLocalDate().plusDays(1).atTime(10, 0)
        shouldThrow<CserealException> {
            reservationService.reserveRoom(request(room.id, start, 2))
        } shouldBe CserealException(ErrorCode.ONE_TIME_RECURRING_DENIED)
    }
}) {
    companion object {
        private fun request(roomId: Long, start: java.time.LocalDateTime, weeks: Int) = ReserveRequest(
            roomId, "title", "a@a.com", "010-1234-5678", "prof", "purpose",
            start, start.plusHours(1), true, weeks
        )

        private fun authenticateLabmaster(repository: UserRepository) {
            val username = "labmaster-policy"
            val user = repository.findByUsername(username) ?: repository.save(
                UserEntity(username, username, "$username@example.com", "0000-00000")
            )
            val authorities = listOf(SimpleGrantedAuthority("ROLE_LABMASTER"))
            val issuedAt = Instant.now()
            val principal = CustomOidcUser(
                user,
                authorities,
                OidcIdToken("mock-token", issuedAt, issuedAt.plusSeconds(3600), mapOf("sub" to username))
            )
            SecurityContextHolder.getContext().authentication =
                UsernamePasswordAuthenticationToken(principal, null, authorities)
        }
    }
}
