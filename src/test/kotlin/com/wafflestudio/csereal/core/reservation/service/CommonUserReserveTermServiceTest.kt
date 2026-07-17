package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.mockauth.CustomOidcUser
import com.wafflestudio.csereal.core.reservation.database.ReservationRepository
import com.wafflestudio.csereal.core.reservation.database.ReservationType
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
import java.time.LocalDateTime

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@Import(MySQLTestContainerConfig::class)
class CommonUserReserveTermServiceTest(
    private val roomRepository: RoomRepository,
    private val reservationRepository: ReservationRepository,
    private val reservationService: ReservationService,
    private val reserveTermPolicy: ReserveTermPolicy,
    private val userRepository: UserRepository
) : BehaviorSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    lateinit var room: RoomEntity

    fun request(
        start: LocalDateTime,
        recurringWeeks: Int = 1,
        type: ReservationType? = null
    ) = ReserveRequest(
        room.id,
        "title",
        "a@a.com",
        "010-1234-5678",
        "prof",
        "purpose",
        start,
        start.plusHours(1),
        true,
        recurringWeeks,
        type
    )

    beforeSpec {
        room = roomRepository.save(RoomEntity("test room", "301", 20, RoomType.SEMINAR))
    }

    beforeTest {
        reservationRepository.deleteAll()
        val user = userRepository.findByUsername("test") ?: userRepository.save(
            UserEntity("test", "test", "test@abc.com", "0000-00000")
        )
        val authorities = listOf(SimpleGrantedAuthority("ROLE_RESERVATION"))
        val issuedAt = Instant.now()
        val token = OidcIdToken("mock-token", issuedAt, issuedAt.plusSeconds(3600), mapOf("sub" to user.username))
        val principal = CustomOidcUser(user, authorities, token)
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(principal, null, authorities)
    }

    given("the legacy one-occurrence request") {
        `when`("a reservation user submits it within the two-week window") {
            val result = reservationService.reserveRoom(request(reserveTermPolicy.now().plusDays(1)))

            then("it remains backward-compatible as an ad-hoc reservation") {
                result.single().reservationType shouldBe ReservationType.AD_HOC
            }
        }
    }

    given("an ad-hoc request more than two weeks away") {
        `when`("the opening date has not arrived") {
            then("the request is rejected") {
                shouldThrow<CserealException> {
                    reservationService.reserveRoom(request(reserveTermPolicy.now().plusDays(15)))
                } shouldBe CserealException(ErrorCode.AD_HOC_NOT_OPENED)
            }
        }
    }

    given("an explicit regular request") {
        `when`("a non-lab-master submits it") {
            then("the request is rejected") {
                shouldThrow<CserealException> {
                    reservationService.reserveRoom(
                        request(reserveTermPolicy.now().plusDays(1), 1, ReservationType.REGULAR)
                    )
                } shouldBe CserealException(ErrorCode.LABMASTER_ONLY)
            }
        }
    }

    given("an explicit recurring ad-hoc request") {
        `when`("it violates the type invariant") {
            then("the request is rejected before occurrence generation") {
                shouldThrow<CserealException> {
                    reservationService.reserveRoom(
                        request(reserveTermPolicy.now().plusDays(1), 2, ReservationType.AD_HOC)
                    )
                } shouldBe CserealException(ErrorCode.AD_HOC_RECURRING_DENIED)
            }
        }
    }
})
