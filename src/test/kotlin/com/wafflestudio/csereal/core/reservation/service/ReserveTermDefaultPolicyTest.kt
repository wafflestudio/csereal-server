package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.core.reservation.database.ReserveTermType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class ReserveTermDefaultPolicyTest : StringSpec({
    "default policy computes scheduler-only canonical windows" {
        val policy = ReserveTermDefaultPolicy(
            Clock.fixed(Instant.parse("2026-07-17T00:00:00Z"), ZoneOffset.UTC)
        )
        val descriptor = policy.descriptor(2027, ReserveTermType.SECOND_SEMESTER)

        descriptor.applyStartTime shouldBe LocalDateTime.of(2027, 8, 2, 0, 0)
        descriptor.applyEndTime shouldBe LocalDateTime.of(2027, 8, 31, 15, 0)
        descriptor.termStartTime shouldBe descriptor.applyEndTime
        descriptor.termEndTime shouldBe LocalDateTime.of(2027, 12, 31, 15, 0)
    }

    "current and next defaults follow Asia/Seoul date boundaries" {
        listOf(
            Instant.parse("2027-02-28T14:59:59Z") to
                listOf(ReserveTermType.WINTER, ReserveTermType.FIRST_SEMESTER),
            Instant.parse("2027-02-28T15:00:00Z") to
                listOf(ReserveTermType.FIRST_SEMESTER, ReserveTermType.SUMMER)
        ).forEach { (instant, expectedTypes) ->
            val policy = ReserveTermDefaultPolicy(Clock.fixed(instant, ZoneOffset.UTC))

            policy.currentAndNextDescriptors().map { it.termType } shouldBe expectedTypes
        }
    }

    "current and next defaults cross the year boundary" {
        val policy = ReserveTermDefaultPolicy(
            Clock.fixed(Instant.parse("2027-12-15T00:00:00Z"), ZoneOffset.UTC)
        )

        policy.currentAndNextDescriptors().map { it.termType } shouldBe
            listOf(ReserveTermType.SECOND_SEMESTER, ReserveTermType.WINTER)
    }
})
