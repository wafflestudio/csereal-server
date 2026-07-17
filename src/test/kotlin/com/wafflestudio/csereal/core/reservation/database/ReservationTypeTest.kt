package com.wafflestudio.csereal.core.reservation.database

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.core.user.database.UserEntity
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime
import java.util.UUID

class ReservationTypeTest : StringSpec({
    "an invalid persisted legacy recurrence fails closed" {
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
            recurringWeeks = 0,
            recurrenceId = UUID.randomUUID(),
            agreed = true,
            reservationType = null
        )

        shouldThrow<CserealException> {
            entity.effectiveType()
        } shouldBe CserealException(ErrorCode.INVALID_RECURRING_WEEKS)
    }
})
