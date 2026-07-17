package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.core.reservation.database.ReservationType
import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermType
import com.wafflestudio.csereal.core.reservation.database.resolvePersistedReservationType
import com.wafflestudio.csereal.core.reservation.database.resolveRequestReservationType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ReserveTermPolicyTest : BehaviorSpec({
    val clock = Clock.fixed(Instant.parse("2026-07-17T00:00:00Z"), ZoneId.of("Asia/Seoul"))
    val policy = ReserveTermPolicy(clock)

    given("a request without an explicit reservation type") {
        then("one occurrence is ad-hoc and multiple occurrences are regular") {
            resolveRequestReservationType(null, 1) shouldBe ReservationType.AD_HOC
            resolveRequestReservationType(null, 2) shouldBe ReservationType.REGULAR
        }

        then("an explicit type wins") {
            resolveRequestReservationType(ReservationType.REGULAR, 1) shouldBe ReservationType.REGULAR
        }

        then("an invalid persisted legacy count remains unclassified") {
            resolvePersistedReservationType(null, 0) shouldBe null
            resolvePersistedReservationType(null, -1) shouldBe null
        }
    }

    given("an ad-hoc opening date on a weekend") {
        then("the opening moves to Monday at 09:00") {
            policy.adHocOpenTime(LocalDateTime.of(2026, 7, 19, 14, 0)) shouldBe
                LocalDateTime.of(2026, 7, 6, 9, 0)
        }
    }

    given("canonical regular terms") {
        then("the second semester opening receives the same weekend adjustment") {
            val descriptor = policy.descriptor(2027, ReserveTermType.SECOND_SEMESTER)
            descriptor.applyStartTime shouldBe LocalDateTime.of(2027, 8, 2, 9, 0)
            descriptor.applyEndTime shouldBe LocalDateTime.of(2028, 1, 1, 0, 0)
            descriptor.termStartTime shouldBe LocalDateTime.of(2027, 9, 1, 0, 0)
            descriptor.termEndTime shouldBe LocalDateTime.of(2028, 1, 1, 0, 0)
        }

        then("adjacent terms can have simultaneously active application windows") {
            val firstSemester = policy.descriptor(2027, ReserveTermType.FIRST_SEMESTER)
            val summer = policy.descriptor(2027, ReserveTermType.SUMMER)
            val overlapTime = LocalDateTime.of(2027, 6, 15, 12, 0)

            (
                !overlapTime.isBefore(firstSemester.applyStartTime) &&
                    overlapTime.isBefore(firstSemester.applyEndTime)
                ) shouldBe true
            (
                !overlapTime.isBefore(summer.applyStartTime) &&
                    overlapTime.isBefore(summer.applyEndTime)
                ) shouldBe true
        }

        then("the current and next terms cross the year boundary") {
            val winterClock = Clock.fixed(Instant.parse("2027-12-15T00:00:00Z"), ZoneId.of("Asia/Seoul"))
            val descriptors = ReserveTermPolicy(winterClock).currentAndNextDescriptors()
            descriptors.map { it.termType } shouldBe listOf(
                ReserveTermType.SECOND_SEMESTER,
                ReserveTermType.WINTER
            )
            descriptors.last().termYear shouldBe 2028
        }
    }

    given("persisted canonical validation") {
        val descriptor = policy.descriptor(2027, ReserveTermType.FIRST_SEMESTER)

        then("all four time fields and metadata must match") {
            val entity = ReserveTermEntity(
                applyStartTime = descriptor.applyStartTime,
                applyEndTime = descriptor.applyEndTime,
                termStartTime = descriptor.termStartTime,
                termEndTime = descriptor.termEndTime,
                termYear = descriptor.termYear,
                termType = descriptor.termType
            )
            policy.audit(descriptor, listOf(entity), listOf(entity)).validEntity shouldBe entity
        }

        then("an apply window mismatch fails closed") {
            val entity = ReserveTermEntity(
                applyStartTime = descriptor.applyStartTime.plusDays(1),
                applyEndTime = descriptor.applyEndTime,
                termStartTime = descriptor.termStartTime,
                termEndTime = descriptor.termEndTime,
                termYear = descriptor.termYear,
                termType = descriptor.termType
            )
            policy.audit(descriptor, listOf(entity), listOf(entity)).reason shouldBe "apply_start_mismatch"
        }

        then("legacy term bounds with a mismatched application window fail closed") {
            val entity = ReserveTermEntity(
                applyStartTime = descriptor.applyStartTime.plusDays(1),
                applyEndTime = descriptor.applyEndTime,
                termStartTime = descriptor.termStartTime,
                termEndTime = descriptor.termEndTime
            )
            policy.audit(descriptor, emptyList(), listOf(entity)).reason shouldBe "apply_start_mismatch"
        }

        then("a matching legacy row is eligible for metadata attachment") {
            val entity = ReserveTermEntity(
                applyStartTime = descriptor.applyStartTime,
                applyEndTime = descriptor.applyEndTime,
                termStartTime = descriptor.termStartTime,
                termEndTime = descriptor.termEndTime
            )
            policy.audit(descriptor, emptyList(), listOf(entity)).validEntity shouldBe entity
        }

        then("competing rows invalidate the descriptor") {
            val first = ReserveTermEntity(
                descriptor.applyStartTime,
                descriptor.applyEndTime,
                descriptor.termStartTime,
                descriptor.termEndTime
            )
            val second = ReserveTermEntity(
                descriptor.applyStartTime,
                descriptor.applyEndTime,
                descriptor.termStartTime,
                descriptor.termEndTime
            )
            policy.audit(descriptor, emptyList(), listOf(first, second)).reason shouldBe "multiple_or_competing_rows"
        }
    }

    given("recurrence bounds") {
        then("the maximum is calculated before untrusted date arithmetic") {
            policy.maxOccurrences(
                LocalDateTime.of(2027, 3, 1, 11, 0),
                LocalDateTime.of(2027, 3, 15, 11, 0)
            ) shouldBe 3
        }
    }
})
