package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.core.reservation.database.ReservationType
import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermRepository
import com.wafflestudio.csereal.core.reservation.database.RoomEntity
import com.wafflestudio.csereal.core.reservation.database.RoomRepository
import com.wafflestudio.csereal.core.reservation.database.RoomType
import com.wafflestudio.csereal.core.reservation.dto.ReserveRequest
import com.wafflestudio.csereal.core.user.database.UserRepository
import com.wafflestudio.csereal.global.authenticateAs
import com.wafflestudio.csereal.global.config.MySQLTestContainerConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@Import(MySQLTestContainerConfig::class)
class CommonUserReserveTermServiceTest(
    private val roomRepository: RoomRepository,
    private val reservationService: ReservationService,
    private val reserveTermPolicy: ReserveTermPolicy,
    private val reserveTermRepository: ReserveTermRepository,
    private val userRepository: UserRepository
) : StringSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))
    lateinit var room: RoomEntity

    beforeSpec {
        room = roomRepository.save(RoomEntity("common room", "301", 20, RoomType.SEMINAR))
    }
    beforeTest {
        val now = reserveTermPolicy.now()
        reserveTermRepository.deleteAll()
        reserveTermRepository.save(
            ReserveTermEntity(now.minusDays(2), now.minusDays(1), now.minusDays(3), now.plusYears(1))
        )
        authenticateAs(userRepository, "common-user", "ROLE_RESERVE")
    }
    afterTest { SecurityContextHolder.clearContext() }

    "a reservation user receives a server-derived ONE_TIME type after the adjusted opening" {
        val start = reserveTermPolicy.now().toLocalDate().plusDays(1).atTime(10, 0)
        reservationService.reserveRoom(request(room.id, start))
            .single().reservationType shouldBe ReservationType.ONE_TIME
    }

    "a reservation user cannot create a recurring non-staff reservation" {
        val start = reserveTermPolicy.now().toLocalDate().plusDays(1).atTime(10, 0)
        shouldThrow<CserealException> {
            reservationService.reserveRoom(request(room.id, start, recurringWeeks = 2))
        } shouldBe CserealException(ErrorCode.ONE_TIME_RECURRING_DENIED)
    }

    "every non-staff occurrence must be at most three hours and on one Seoul date" {
        val start = reserveTermPolicy.now().plusDays(1).withHour(22)
        shouldThrow<CserealException> {
            reservationService.reserveRoom(request(room.id, start, end = start.plusHours(3).plusMinutes(1)))
        } shouldBe CserealException(ErrorCode.RESERVATION_TIME_EXCEEDED)
    }
}) {
    companion object {
        private fun request(
            roomId: Long,
            start: java.time.LocalDateTime,
            end: java.time.LocalDateTime = start.plusHours(1),
            recurringWeeks: Int = 1
        ) = ReserveRequest(
            roomId, "title", "a@a.com", "010-1234-5678", "prof", "purpose",
            start, end, true, recurringWeeks
        )
    }
}
