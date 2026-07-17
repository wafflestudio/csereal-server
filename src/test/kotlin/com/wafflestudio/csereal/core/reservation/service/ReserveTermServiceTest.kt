package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.mockauth.CustomOidcUser
import com.wafflestudio.csereal.core.reservation.database.ReservationRepository
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
import io.kotest.core.spec.style.BehaviorSpec
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
    private val reservationRepository: ReservationRepository,
    private val reservationService: ReservationService,
    private val reserveTermRepository: ReserveTermRepository,
    private val reserveTermPolicy: ReserveTermPolicy,
    private val userRepository: UserRepository
) : BehaviorSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    lateinit var room: RoomEntity

    fun saveTerm(descriptor: ReserveTermDescriptor) {
        reserveTermRepository.save(
            ReserveTermEntity(
                descriptor.applyStartTime,
                descriptor.applyEndTime,
                descriptor.termStartTime,
                descriptor.termEndTime,
                descriptor.termYear,
                descriptor.termType
            )
        )
    }

    fun request(
        start: java.time.LocalDateTime,
        end: java.time.LocalDateTime,
        recurringWeeks: Int = 1,
        type: ReservationType? = ReservationType.REGULAR
    ) = ReserveRequest(
        room.id,
        "title",
        "a@a.com",
        "010-1234-5678",
        "prof",
        "purpose",
        start,
        end,
        true,
        recurringWeeks,
        type
    )

    beforeSpec {
        reserveTermRepository.deleteAll()
        room = roomRepository.save(RoomEntity("test room", "301", 20, RoomType.SEMINAR))
    }

    beforeTest {
        reservationRepository.deleteAll()
        val mockUser = userRepository.findByUsername("test") ?: userRepository.save(
            UserEntity("test", "test", "test@abc.com", "0000-00000")
        )
        val authorities = listOf(SimpleGrantedAuthority("ROLE_LABMASTER"))
        val issuedAt = Instant.now()
        val token = OidcIdToken("mock-token", issuedAt, issuedAt.plusSeconds(3600), mapOf("sub" to mockUser.username))
        val principal = CustomOidcUser(mockUser, authorities, token)
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(principal, null, authorities)
    }

    given("a validated canonical term whose application window is open") {
        val now = reserveTermPolicy.now()
        val current = reserveTermPolicy.descriptorFor(now.toLocalDate())
        val next = reserveTermPolicy.descriptorFor(current.termEndTime.toLocalDate())
        val descriptor = listOf(current, next).first {
            !now.isBefore(it.applyStartTime) && now.isBefore(it.applyEndTime) &&
                now.plusDays(22).isBefore(it.termEndTime)
        }
        saveTerm(descriptor)
        val start = maxOf(now.plusDays(1), descriptor.termStartTime.plusDays(1)).withHour(10).withMinute(0)

        `when`("a lab master creates a regular recurring reservation") {
            val result = reservationService.reserveRoom(request(start, start.plusHours(1), 3))

            then("all occurrences are saved with the regular type") {
                result.size shouldBe 3
                result.all { it.reservationType == ReservationType.REGULAR } shouldBe true
            }
        }

        `when`("a lab master creates a one-occurrence regular reservation") {
            val result = reservationService.reserveRoom(request(start, start.plusHours(1)))

            then("the explicit regular type is retained") {
                result.single().reservationType shouldBe ReservationType.REGULAR
            }
        }

        `when`("a non-staff regular reservation exceeds three hours") {
            then("the request is rejected") {
                shouldThrow<CserealException> {
                    reservationService.reserveRoom(request(start, start.plusHours(4)))
                } shouldBe CserealException(ErrorCode.RESERVATION_TIME_EXCEEDED)
            }
        }
    }

    given("a canonical term whose application has not opened") {
        val current = reserveTermPolicy.descriptorFor(reserveTermPolicy.now().toLocalDate())
        val next = reserveTermPolicy.descriptorFor(current.termEndTime.toLocalDate())
        val future = reserveTermPolicy.descriptorFor(next.termEndTime.toLocalDate())
        saveTerm(future)

        `when`("a lab master requests its regular period") {
            then("the request fails closed") {
                shouldThrow<CserealException> {
                    reservationService.reserveRoom(
                        request(
                            future.termStartTime.plusDays(1).plusHours(10),
                            future.termStartTime.plusDays(1).plusHours(11)
                        )
                    )
                } shouldBe CserealException(ErrorCode.TERM_NOT_OPENED)
            }
        }
    }

    given("a past request") {
        `when`("a lab master submits it") {
            then("the universal invariant rejects it") {
                shouldThrow<CserealException> {
                    reservationService.reserveRoom(
                        request(reserveTermPolicy.now().minusHours(2), reserveTermPolicy.now().minusHours(1))
                    )
                } shouldBe CserealException(ErrorCode.PAST_RESERVATION_DENIED)
            }
        }
    }
})
