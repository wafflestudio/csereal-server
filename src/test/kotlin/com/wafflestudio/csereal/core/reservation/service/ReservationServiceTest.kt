package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.core.reservation.database.*
import com.wafflestudio.csereal.core.reservation.dto.ReserveRequest
import com.wafflestudio.csereal.core.user.database.UserEntity
import com.wafflestudio.csereal.core.user.database.UserRepository
import com.wafflestudio.csereal.global.config.MySQLTestContainerConfig
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@Import(MySQLTestContainerConfig::class)
class ReservationServiceTest(
    private val roomRepository: RoomRepository,
    private val reservationService: ReservationService,
    private val userRepository: UserRepository
) : BehaviorSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    lateinit var dummyRoom: RoomEntity

    beforeSpec {
        dummyRoom = roomRepository.save(RoomEntity("test room", "301", 20, RoomType.SEMINAR))
        if (userRepository.findByUsername("test") == null) {
            userRepository.save(
                UserEntity(
                    "test",
                    "test",
                    "test@abc.com",
                    "0000-00000"
                )
            )
        }
    }

    beforeTest {
        SecurityContextHolder.clearContext()
    }

    given("Staff reservation invariants and overrides") {
        fun request(
            start: LocalDateTime,
            end: LocalDateTime,
            recurringWeeks: Int,
            type: ReservationType
        ) = ReserveRequest(
            dummyRoom.id,
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

        `when`("staff submits a recurring ad-hoc request") {
            then("the type invariant is still enforced") {
                io.kotest.assertions.throwables.shouldThrow<CserealException> {
                    val start = LocalDateTime.now().plusDays(1)
                    reservationService.reserveRoom(request(start, start.plusHours(1), 2, ReservationType.AD_HOC))
                } shouldBe CserealException(ErrorCode.AD_HOC_RECURRING_DENIED)
            }
        }

        `when`("staff submits a past request") {
            then("the universal future-time invariant is enforced") {
                io.kotest.assertions.throwables.shouldThrow<CserealException> {
                    val start = LocalDateTime.now().minusHours(2)
                    reservationService.reserveRoom(request(start, start.plusHours(1), 1, ReservationType.AD_HOC))
                } shouldBe CserealException(ErrorCode.PAST_RESERVATION_DENIED)
            }
        }

        `when`("staff submits dates outside the supported database range") {
            then("lower and upper years fail with a stable domain error") {
                listOf(1000, 9999).forEach { year ->
                    val start = LocalDateTime.of(year, 1, 10, 10, 0)
                    io.kotest.assertions.throwables.shouldThrow<CserealException> {
                        reservationService.reserveRoom(
                            request(start, start.plusHours(1), 1, ReservationType.AD_HOC)
                        )
                    } shouldBe CserealException(ErrorCode.UNSUPPORTED_RESERVATION_DATE)
                }
            }
        }

        `when`("staff uses the inclusive supported year boundaries") {
            then("the lower boundary reaches the past invariant and the upper boundary is accepted") {
                val lower = LocalDateTime.of(1001, 1, 10, 10, 0)
                io.kotest.assertions.throwables.shouldThrow<CserealException> {
                    reservationService.reserveRoom(
                        request(lower, lower.plusHours(1), 1, ReservationType.AD_HOC)
                    )
                } shouldBe CserealException(ErrorCode.PAST_RESERVATION_DENIED)

                val upper = LocalDateTime.of(9998, 12, 20, 10, 0)
                reservationService.reserveRoom(
                    request(upper, upper.plusHours(1), 1, ReservationType.AD_HOC)
                ).size shouldBe 1
            }
        }

        `when`("staff repeats a reservation across the supported upper boundary") {
            then("the request fails before plusWeeks or persistence") {
                val start = LocalDateTime.of(9998, 12, 28, 10, 0)
                io.kotest.assertions.throwables.shouldThrow<CserealException> {
                    reservationService.reserveRoom(
                        request(start, start.plusHours(1), 2, ReservationType.REGULAR)
                    )
                } shouldBe CserealException(ErrorCode.UNSUPPORTED_RESERVATION_DATE)
            }
        }

        `when`("staff submits a regular request outside a canonical term policy") {
            val start = LocalDateTime.now().plusDays(3)
            val result = reservationService.reserveRoom(request(start, start.plusHours(4), 2, ReservationType.REGULAR))

            then("business time and term policies are bypassed") {
                result.size shouldBe 2
            }
        }
    }
})
