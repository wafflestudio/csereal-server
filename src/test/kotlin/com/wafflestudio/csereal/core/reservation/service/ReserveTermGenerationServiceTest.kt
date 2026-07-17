package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.core.reservation.database.ReserveTermType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.dao.DataIntegrityViolationException
import java.time.LocalDateTime

class ReserveTermGenerationServiceTest : BehaviorSpec({
    fun descriptor(year: Int, type: ReserveTermType) = ReserveTermDescriptor(
        year,
        type,
        LocalDateTime.of(year, 1, 1, 9, 0),
        LocalDateTime.of(year, 3, 1, 0, 0),
        LocalDateTime.of(year, 1, 1, 0, 0),
        LocalDateTime.of(year, 3, 1, 0, 0)
    )

    given("two descriptors reconciled in independent transactions") {
        val current = descriptor(2027, ReserveTermType.WINTER)
        val next = descriptor(2027, ReserveTermType.FIRST_SEMESTER)
        val policy = mockk<ReserveTermPolicy>()
        val reconciliation = mockk<ReserveTermReconciliationService>()
        every { policy.currentAndNextDescriptors() } returns listOf(current, next)
        every { reconciliation.ensureTerm(current) } throws InvalidReserveTermStateException(
            ReserveTermAudit(current, emptyList(), null, "apply_start_mismatch")
        )
        every { reconciliation.ensureTerm(next) } returns ReserveTermReconciliationResult.CREATED
        val service = ReserveTermGenerationService(policy, reconciliation)

        `when`("the current term fails") {
            val outcomes = service.ensureCurrentAndNext()

            then("the next term is still reconciled") {
                (outcomes.first().error is InvalidReserveTermStateException) shouldBe true
                outcomes.last().result shouldBe ReserveTermReconciliationResult.CREATED
                verify(exactly = 1) { reconciliation.ensureTerm(next) }
            }
        }
    }

    given("a concurrent unique-key race") {
        val current = descriptor(2027, ReserveTermType.WINTER)
        val policy = mockk<ReserveTermPolicy>()
        val reconciliation = mockk<ReserveTermReconciliationService>()
        every { policy.currentAndNextDescriptors() } returns listOf(current)
        every { reconciliation.ensureTerm(current) } throws DataIntegrityViolationException("duplicate")
        every { reconciliation.verifyAfterConcurrentInsert(current) } returns
            ReserveTermReconciliationResult.CONCURRENTLY_CREATED
        val service = ReserveTermGenerationService(policy, reconciliation)

        then("the row is re-audited after rollback") {
            service.ensureCurrentAndNext().single().result shouldBe
                ReserveTermReconciliationResult.CONCURRENTLY_CREATED
            verify(exactly = 1) { reconciliation.verifyAfterConcurrentInsert(current) }
        }
    }
})
