package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.core.reservation.config.ReservationConfig
import com.wafflestudio.csereal.core.reservation.config.ReservationProperties
import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
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

    given("UTC component clock basis") {
        then("now is derived from the clock instant in UTC") {
            policy.now() shouldBe LocalDateTime.of(2026, 7, 17, 0, 0)
        }

        then("the default phase comparison uses the same UTC component basis") {
            val term = ReserveTermEntity(
                LocalDateTime.of(2026, 7, 17, 0, 0),
                LocalDateTime.of(2026, 7, 17, 1, 0),
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 9, 1, 0, 0)
            )

            policy.phase(term) shouldBe ReserveTermPhase.REGULAR_APPLICATION
        }
    }

    given("persisted schedule validation and phases") {
        val term = ReserveTermEntity(
            LocalDateTime.of(2027, 2, 3, 0, 0),
            LocalDateTime.of(2027, 2, 20, 9, 0),
            LocalDateTime.of(2027, 2, 28, 15, 0),
            LocalDateTime.of(2027, 6, 24, 15, 0)
        )

        then("a non-overlapping application window is valid with half-open boundaries") {
            policy.invalidReasons(term) shouldBe emptyList()
            policy.phase(term, term.applyStartTime.minusNanos(1)) shouldBe ReserveTermPhase.BEFORE_APPLICATION
            policy.phase(term, term.applyStartTime) shouldBe ReserveTermPhase.REGULAR_APPLICATION
            policy.phase(term, term.applyEndTime) shouldBe ReserveTermPhase.GAP
            policy.phase(term, term.termStartTime) shouldBe ReserveTermPhase.TERM_ACTIVE
        }

        then("an application window may overlap the start of its term") {
            val overlapping = ReserveTermEntity(
                term.applyStartTime,
                term.termStartTime.plusDays(2),
                term.termStartTime,
                term.termEndTime
            )

            policy.invalidReasons(overlapping) shouldBe emptyList()
            policy.phase(overlapping, overlapping.termStartTime) shouldBe ReserveTermPhase.REGULAR_APPLICATION
            policy.phase(overlapping, overlapping.applyEndTime) shouldBe ReserveTermPhase.TERM_ACTIVE
        }

        then("a term-internal application window takes priority only while open") {
            val internal = ReserveTermEntity(
                term.termStartTime.plusDays(2),
                term.termStartTime.plusDays(5),
                term.termStartTime,
                term.termEndTime
            )

            policy.invalidReasons(internal) shouldBe emptyList()
            policy.phase(internal, internal.applyStartTime.minusNanos(1)) shouldBe ReserveTermPhase.TERM_ACTIVE
            policy.phase(internal, internal.applyStartTime) shouldBe ReserveTermPhase.REGULAR_APPLICATION
            policy.phase(internal, internal.applyEndTime.minusNanos(1)) shouldBe ReserveTermPhase.REGULAR_APPLICATION
            policy.phase(internal, internal.applyEndTime) shouldBe ReserveTermPhase.TERM_ACTIVE
        }

        then("an application window cannot end after its term") {
            policy.invalidReasons(
                ReserveTermEntity(
                    term.applyStartTime,
                    term.termEndTime.plusNanos(1),
                    term.termStartTime,
                    term.termEndTime
                )
            ) shouldBe listOf("application_ends_after_term")
        }

        then("partial metadata and malformed time order are invalid") {
            policy.invalidReasons(
                ReserveTermEntity(
                    term.applyEndTime,
                    term.applyStartTime,
                    term.termStartTime,
                    term.termEndTime,
                    2027,
                    null
                )
            ) shouldBe listOf("invalid_application_window", "partial_metadata")
        }
    }

    given("one-time opening") {
        then("a UTC reservation start uses the Asia/Seoul calendar and returns a UTC opening") {
            val reservationStartUtc = LocalDateTime.of(2026, 7, 17, 16, 0)

            policy.oneTimeReservationOpenTime(reservationStartUtc) shouldBe
                LocalDateTime.of(2026, 7, 6, 0, 0)
        }

        then("active terms cannot open before their persisted start") {
            val term = ReserveTermEntity(
                LocalDateTime.of(2027, 1, 1, 0, 0),
                LocalDateTime.of(2027, 1, 14, 15, 0),
                LocalDateTime.of(2027, 2, 28, 15, 0),
                LocalDateTime.of(2027, 6, 30, 15, 0)
            )
            policy.activeTermOneTimeReservationOpenTime(term, LocalDateTime.of(2027, 3, 2, 1, 0)) shouldBe
                term.termStartTime
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
        val contextRunner = ApplicationContextRunner().withUserConfiguration(ReservationConfig::class.java)

        then("the default maximum is 20") {
            contextRunner.run { context ->
                context.startupFailure shouldBe null
                context.getBean(ReservationProperties::class.java).maxRecurringWeeks shouldBe 20
            }
        }

        then("a positive override is accepted") {
            contextRunner.withPropertyValues("csereal.reservation.max-recurring-weeks=24").run { context ->
                context.startupFailure shouldBe null
                context.getBean(ReservationProperties::class.java).maxRecurringWeeks shouldBe 24
            }
        }
    }
})
