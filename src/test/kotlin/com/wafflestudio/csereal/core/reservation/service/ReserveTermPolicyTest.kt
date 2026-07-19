package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.core.reservation.config.ReservationConfig
import com.wafflestudio.csereal.core.reservation.config.ReservationProperties
import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ReserveTermPolicyTest : BehaviorSpec({
    val clock = Clock.fixed(Instant.parse("2026-07-17T00:00:00Z"), ZoneId.of("Asia/Seoul"))
    val policy = ReserveTermPolicy(clock)

    given("canonical timing") {
        then("application ends exactly when the target term starts") {
            val descriptor = policy.descriptor(2027, ReserveTermType.SECOND_SEMESTER)
            descriptor.applyStartTime shouldBe LocalDateTime.of(2027, 8, 2, 9, 0)
            descriptor.applyEndTime shouldBe descriptor.termStartTime
            descriptor.termStartTime shouldBe LocalDateTime.of(2027, 9, 1, 0, 0)
            descriptor.termEndTime shouldBe LocalDateTime.of(2028, 1, 1, 0, 0)
        }

        then("an ad-hoc weekend opening moves to Monday at 09:00") {
            policy.adHocOpenTime(LocalDateTime.of(2026, 7, 19, 14, 0)) shouldBe
                LocalDateTime.of(2026, 7, 6, 9, 0)
        }

        then("current and next terms cross the year boundary") {
            val winterClock = Clock.fixed(Instant.parse("2027-12-15T00:00:00Z"), ZoneId.of("Asia/Seoul"))
            ReserveTermPolicy(winterClock).currentAndNextDescriptors().map { it.termType } shouldBe
                listOf(ReserveTermType.SECOND_SEMESTER, ReserveTermType.WINTER)
        }
    }

    given("persisted canonical validation") {
        val descriptor = policy.descriptor(2027, ReserveTermType.FIRST_SEMESTER)

        then("metadata and all four time fields match") {
            val entity = ReserveTermEntity(
                descriptor.applyStartTime,
                descriptor.applyEndTime,
                descriptor.termStartTime,
                descriptor.termEndTime,
                descriptor.termYear,
                descriptor.termType
            )
            policy.audit(descriptor, listOf(entity), listOf(entity)).validEntity shouldBe entity
        }

        then("a field mismatch fails closed") {
            val entity = ReserveTermEntity(
                descriptor.applyStartTime,
                descriptor.applyEndTime.plusMinutes(1),
                descriptor.termStartTime,
                descriptor.termEndTime,
                descriptor.termYear,
                descriptor.termType
            )
            policy.audit(descriptor, listOf(entity), listOf(entity)).reason shouldBe "apply_end_mismatch"
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

    given("reservation property binding") {
        val contextRunner = ApplicationContextRunner()
            .withUserConfiguration(ReservationConfig::class.java)

        then("the default maximum is 15") {
            contextRunner.run { context ->
                context.startupFailure shouldBe null
                context.getBean(ReservationProperties::class.java).maxRecurringWeeks shouldBe 15
            }
        }

        then("a positive override is accepted") {
            contextRunner.withPropertyValues("csereal.reservation.max-recurring-weeks=24").run { context ->
                context.startupFailure shouldBe null
                context.getBean(ReservationProperties::class.java).maxRecurringWeeks shouldBe 24
            }
        }

        then("zero and negative values reject startup binding") {
            listOf(0, -1).forEach { invalidValue ->
                contextRunner.withPropertyValues(
                    "csereal.reservation.max-recurring-weeks=$invalidValue"
                ).run { context ->
                    (context.startupFailure != null) shouldBe true
                }
            }
        }
    }
})
