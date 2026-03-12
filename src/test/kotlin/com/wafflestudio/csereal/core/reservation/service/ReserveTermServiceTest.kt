package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.mockauth.CustomOidcUser
import com.wafflestudio.csereal.core.reservation.database.ReservationRepository
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
import java.time.LocalDateTime

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@Import(MySQLTestContainerConfig::class)
class ReserveTermServiceTest(
    private val roomRepository: RoomRepository,
    private val reservationRepository: ReservationRepository,
    private val reservationService: ReservationService,
    private val reserveTermRepository: ReserveTermRepository,
    private val userRepository: UserRepository
) : BehaviorSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    lateinit var testSeminarRoom: RoomEntity

    beforeSpec {
        testSeminarRoom = roomRepository.save(RoomEntity("test room", "301", 20, RoomType.SEMINAR))
    }

    beforeTest {
        reservationRepository.deleteAll()

        // 랩 대표 권한이 있는 유저로 테스트
        val mockUser = userRepository.findByUsername("test")
            ?: userRepository.save(
                UserEntity(
                    "test",
                    "test",
                    "test@abc.com",
                    "0000-00000"
                )
            )

        val authorities = listOf(SimpleGrantedAuthority("ROLE_LABMASTER"))
        val issuedAt = Instant.now()
        val expiresAt = issuedAt.plusSeconds(3600)
        val claims = mapOf("sub" to mockUser.username)
        val dummyIdToken = OidcIdToken("mock-token", issuedAt, expiresAt, claims)

        val customOidcUser = CustomOidcUser(mockUser, authorities, dummyIdToken)
        val authentication = UsernamePasswordAuthenticationToken(customOidcUser, null, authorities)

        SecurityContextHolder.getContext().authentication = authentication
    }

    given("Pre-Reservation is Allowed Now") {
        val termStartTime = LocalDateTime.now().plusMonths(1)
        val termEndTime = LocalDateTime.now().plusMonths(1).plusDays(30)
        reserveTermRepository.deleteAll()
        reserveTermRepository.save(
            ReserveTermEntity(
                applyStartTime = LocalDateTime.now().minusDays(1),
                applyEndTime = LocalDateTime.now().plusDays(1),
                termStartTime = termStartTime,
                termEndTime = termEndTime
            )
        )

        `when`("requested reservation time fits in the term") {
            val startTime = termStartTime.plusDays(1).withHour(10)
            val endTime = termStartTime.plusDays(1).withHour(11)
            val reserveRequest =
                ReserveRequest(
                    testSeminarRoom.id,
                    "title",
                    "a@a.com",
                    "010-1234-5678",
                    "prof",
                    "purp",
                    startTime,
                    endTime,
                    true,
                    3
                )

            val result = reservationService.reserveRoom(reserveRequest)

            then("create reservations successfully") {
                result.size shouldBe 3
            }
        }

        `when`("requested reservation time doesn't fit in the term") {
            val startTime = termStartTime.minusDays(1).withHour(10)
            val endTime = termStartTime.minusDays(1).withHour(11)
            val reserveRequest =
                ReserveRequest(
                    testSeminarRoom.id,
                    "title",
                    "a@a.com",
                    "010-1234-5678",
                    "prof",
                    "purp",
                    startTime,
                    endTime,
                    true,
                    3
                )

            then("fail to make reservations out of the term") {
                shouldThrow<CserealException> {
                    reservationService.reserveRoom(reserveRequest)
                }

                val reservations = reservationRepository.findAll()
                reservations.size shouldBe 0
            }
        }

        `when`("reservation duration exceeds 3 hours") {
            val startTime = termStartTime.plusDays(1).withHour(10)
            val endTime = termStartTime.plusDays(1).withHour(14)
            val reserveRequest =
                ReserveRequest(
                    testSeminarRoom.id,
                    "title",
                    "a@a.com",
                    "010-1234-5678",
                    "prof",
                    "purp",
                    startTime,
                    endTime,
                    true,
                    3
                )

            then("fail to make reservations too long") {
                shouldThrow<CserealException> {
                    reservationService.reserveRoom(reserveRequest)
                }

                val reservations = reservationRepository.findAll()
                reservations.size shouldBe 0
            }
        }
    }

    given("Pre-Reservation apply time passed") {
        val termStartTime = LocalDateTime.now().plusMonths(1)
        val termEndTime = LocalDateTime.now().plusMonths(1).plusDays(30)
        reserveTermRepository.deleteAll()
        reserveTermRepository.save(
            ReserveTermEntity(
                applyStartTime = LocalDateTime.now().minusDays(3),
                applyEndTime = LocalDateTime.now().minusDays(1),
                termStartTime = termStartTime,
                termEndTime = termEndTime
            )
        )

        `when`("leader tries reservation") {
            val startTime = termStartTime.plusDays(1).withHour(10)
            val endTime = termStartTime.plusDays(1).withHour(11)
            val reserveRequest =
                ReserveRequest(
                    testSeminarRoom.id,
                    "title",
                    "a@a.com",
                    "010-1234-5678",
                    "prof",
                    "purp",
                    startTime,
                    endTime,
                    true,
                    3
                )

            then("success to reserve") {
                reservationService.reserveRoom(reserveRequest)
                val reservations = reservationRepository.findAll()
                reservations.size shouldBe 3
            }
        }

        `when`("leader tries reservation after registered terms") {
            val startTime = termEndTime.plusDays(7).withHour(10)
            val endTime = termEndTime.plusDays(7).withHour(11)
            val reserveRequest =
                ReserveRequest(
                    testSeminarRoom.id,
                    "title",
                    "a@a.com",
                    "010-1234-5678",
                    "prof",
                    "purp",
                    startTime,
                    endTime,
                    true
                )

            then("cannot make reservations after registered terms") {
                shouldThrow<CserealException> {
                    reservationService.reserveRoom(reserveRequest)
                }
                val reservations = reservationRepository.findAll()
                reservations.size shouldBe 0
            }
        }
    }

    given("Pre-Reservation apply time didn't start") {
        val termStartTime = LocalDateTime.now().plusMonths(1)
        val termEndTime = LocalDateTime.now().plusMonths(1).plusDays(30)
        reserveTermRepository.deleteAll()
        reserveTermRepository.save(
            ReserveTermEntity(
                applyStartTime = LocalDateTime.now().plusDays(1),
                applyEndTime = LocalDateTime.now().plusDays(3),
                termStartTime = termStartTime,
                termEndTime = termEndTime
            )
        )
        `when`("leader tries reservation") {
            val startTime = termStartTime.plusDays(1).withHour(10)
            val endTime = termStartTime.plusDays(1).withHour(11)
            val reserveRequest =
                ReserveRequest(
                    testSeminarRoom.id,
                    "title",
                    "a@a.com",
                    "010-1234-5678",
                    "prof",
                    "purp",
                    startTime,
                    endTime,
                    true,
                    3
                )

            then("cannot make reservations before apply-start-time") {
                shouldThrow<CserealException> {
                    reservationService.reserveRoom(reserveRequest)
                }
                val reservations = reservationRepository.findAll()
                reservations.size shouldBe 0
            }
        }
    }
})
