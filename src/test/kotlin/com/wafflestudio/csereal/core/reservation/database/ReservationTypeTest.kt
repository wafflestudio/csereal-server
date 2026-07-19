package com.wafflestudio.csereal.core.reservation.database

import com.wafflestudio.csereal.core.reservation.dto.ReservationDto
import com.wafflestudio.csereal.core.user.database.UserEntity
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime
import java.util.UUID

class ReservationTypeTest : StringSpec({
    "legacy reservation types remain nullable in responses" {
        val entity = ReservationEntity(
            user = UserEntity("legacy", "legacy", "legacy@example.com", "0000-00000"),
            room = RoomEntity("room", "301", 10, RoomType.SEMINAR),
            title = "legacy",
            contactEmail = "legacy@example.com",
            contactPhone = "010-0000-0000",
            purpose = "legacy",
            startTime = LocalDateTime.of(2027, 3, 10, 10, 0),
            endTime = LocalDateTime.of(2027, 3, 10, 11, 0),
            professor = "professor",
            recurringWeeks = 1,
            recurrenceId = UUID.randomUUID(),
            agreed = true,
            reservationType = null
        )

        ReservationDto.of(entity).reservationType shouldBe null
        ReservationType.entries shouldBe listOf(
            ReservationType.AD_HOC,
            ReservationType.REGULAR,
            ReservationType.UNRESTRICTED
        )
    }
})
