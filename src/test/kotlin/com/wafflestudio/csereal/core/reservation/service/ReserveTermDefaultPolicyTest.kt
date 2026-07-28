package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.core.reservation.database.ReserveTermType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ReserveTermDefaultPolicyTest : StringSpec({
    "default policy computes scheduler-only canonical windows" {
        val policy = ReserveTermDefaultPolicy(
            Clock.fixed(Instant.parse("2026-07-17T00:00:00Z"), ZoneId.of("Asia/Seoul"))
        )
        val descriptor = policy.descriptor(2027, ReserveTermType.SECOND_SEMESTER)

        descriptor.applyStartTime shouldBe LocalDateTime.of(2027, 8, 2, 9, 0)
        descriptor.applyEndTime shouldBe LocalDateTime.of(2027, 9, 1, 0, 0)
        descriptor.termStartTime shouldBe descriptor.applyEndTime
        descriptor.termEndTime shouldBe LocalDateTime.of(2028, 1, 1, 0, 0)
    }

    "current and next defaults cross the year boundary" {
        val policy = ReserveTermDefaultPolicy(
            Clock.fixed(Instant.parse("2027-12-15T00:00:00Z"), ZoneId.of("Asia/Seoul"))
        )

        policy.currentAndNextDescriptors().map { it.termType } shouldBe
            listOf(ReserveTermType.SECOND_SEMESTER, ReserveTermType.WINTER)
    }
})
