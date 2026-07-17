package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermRepository
import com.wafflestudio.csereal.core.reservation.database.ReserveTermType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class ReserveTermReconciliationServiceTest : BehaviorSpec({
    val clock = Clock.fixed(Instant.parse("2026-07-17T00:00:00Z"), ZoneId.of("Asia/Seoul"))
    val policy = ReserveTermPolicy(clock)

    fun services(
        repository: ReserveTermRepository
    ): Pair<ReserveTermValidationService, ReserveTermReconciliationService> {
        val validation = ReserveTermValidationService(repository, policy)
        return validation to ReserveTermReconciliationService(repository, validation)
    }

    given("one exact legacy row") {
        val repository = mockk<ReserveTermRepository>()
        val descriptor = policy.descriptor(2027, ReserveTermType.FIRST_SEMESTER)
        val legacy = ReserveTermEntity(
            descriptor.applyStartTime,
            descriptor.applyEndTime,
            descriptor.termStartTime,
            descriptor.termEndTime
        )
        every { repository.findByTermYearAndTermType(descriptor.termYear, descriptor.termType) } returns emptyList()
        every { repository.findByTimeOverlap(descriptor.termStartTime, descriptor.termEndTime) } returns listOf(legacy)
        every { repository.saveAndFlush(legacy) } returns legacy
        val (_, reconciliation) = services(repository)

        `when`("the term is reconciled") {
            val result = reconciliation.ensureTerm(descriptor)

            then("only canonical metadata is attached") {
                result shouldBe ReserveTermReconciliationResult.METADATA_ATTACHED
                legacy.termYear shouldBe descriptor.termYear
                legacy.termType shouldBe descriptor.termType
                verify(exactly = 1) { repository.saveAndFlush(legacy) }
            }
        }
    }

    given("a keyed row with an application-window mismatch") {
        val repository = mockk<ReserveTermRepository>()
        val descriptor = policy.descriptor(2027, ReserveTermType.SUMMER)
        val invalid = ReserveTermEntity(
            descriptor.applyStartTime,
            descriptor.applyEndTime.minusDays(1),
            descriptor.termStartTime,
            descriptor.termEndTime,
            descriptor.termYear,
            descriptor.termType
        )
        every { repository.findByTermYearAndTermType(descriptor.termYear, descriptor.termType) } returns listOf(invalid)
        every { repository.findByTimeOverlap(descriptor.termStartTime, descriptor.termEndTime) } returns listOf(invalid)
        val (_, reconciliation) = services(repository)

        `when`("the term is reconciled") {
            then("the row is preserved and reconciliation fails closed") {
                shouldThrow<InvalidReserveTermStateException> {
                    reconciliation.ensureTerm(descriptor)
                }.audit.reason shouldBe "apply_end_mismatch"
                verify(exactly = 0) { repository.saveAndFlush(any()) }
            }
        }
    }

    given("a keyed row outside its canonical term window") {
        val repository = mockk<ReserveTermRepository>()
        val descriptor = policy.descriptor(2027, ReserveTermType.WINTER)
        val invalid = ReserveTermEntity(
            descriptor.applyStartTime,
            descriptor.applyEndTime,
            descriptor.termStartTime.plusYears(1),
            descriptor.termEndTime.plusYears(1),
            descriptor.termYear,
            descriptor.termType
        )
        every { repository.findByTermYearAndTermType(descriptor.termYear, descriptor.termType) } returns listOf(invalid)
        every { repository.findByTimeOverlap(descriptor.termStartTime, descriptor.termEndTime) } returns emptyList()
        val (_, reconciliation) = services(repository)

        then("the keyed lookup still detects the mismatch") {
            shouldThrow<InvalidReserveTermStateException> {
                reconciliation.ensureTerm(descriptor)
            }.audit.reason shouldBe "term_start_mismatch"
        }
    }

    given("no persisted row") {
        val repository = mockk<ReserveTermRepository>()
        val descriptor = policy.descriptor(2027, ReserveTermType.SECOND_SEMESTER)
        every { repository.findByTermYearAndTermType(descriptor.termYear, descriptor.termType) } returns emptyList()
        every { repository.findByTimeOverlap(descriptor.termStartTime, descriptor.termEndTime) } returns emptyList()
        every { repository.saveAndFlush(any()) } answers { firstArg() }
        val (_, reconciliation) = services(repository)

        then("a canonical row is inserted") {
            reconciliation.ensureTerm(descriptor) shouldBe ReserveTermReconciliationResult.CREATED
            verify(exactly = 1) {
                repository.saveAndFlush(
                    match {
                        it.termYear == descriptor.termYear &&
                            it.termType == descriptor.termType &&
                            it.applyStartTime == descriptor.applyStartTime &&
                            it.applyEndTime == descriptor.applyEndTime
                    }
                )
            }
        }
    }
})
