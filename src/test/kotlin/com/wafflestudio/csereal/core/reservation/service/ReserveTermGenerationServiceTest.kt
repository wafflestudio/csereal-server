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
        LocalDateTime.of(year, 1, 15, 0, 0),
        LocalDateTime.of(year, 2, 1, 0, 0),
        LocalDateTime.of(year, 3, 1, 0, 0)
    )

    given("two defaults handled independently") {
        val current = descriptor(2027, ReserveTermType.WINTER)
        val next = descriptor(2027, ReserveTermType.FIRST_SEMESTER)
        val defaultPolicy = mockk<ReserveTermDefaultPolicy>()
        val creation = mockk<ReserveTermCreationService>()
        every { defaultPolicy.currentAndNextDescriptors() } returns listOf(current, next)
        every { creation.createDefault(current) } throws IllegalStateException("failed current")
        every { creation.createDefault(next) } returns ReserveTermCreationDecision(ReserveTermGenerationResult.CREATED)
        val service = ReserveTermGenerationService(defaultPolicy, creation)

        `when`("the current default fails") {
            val outcomes = service.ensureCurrentAndNext()

            then("the next default is still created") {
                outcomes.first().result shouldBe ReserveTermGenerationResult.FAILED
                outcomes.last().result shouldBe ReserveTermGenerationResult.CREATED
                verify(exactly = 1) { creation.createDefault(next) }
            }
        }
    }

    given("an insert integrity race") {
        val current = descriptor(2027, ReserveTermType.WINTER)
        val defaultPolicy = mockk<ReserveTermDefaultPolicy>()
        val creation = mockk<ReserveTermCreationService>()
        every { defaultPolicy.currentAndNextDescriptors() } returns listOf(current)
        every { creation.createDefault(current) } throws DataIntegrityViolationException("duplicate")
        every { creation.inspectAfterIntegrityFailure(current) } returns
            ReserveTermCreationDecision(ReserveTermGenerationResult.CONCURRENTLY_CREATED)
        val service = ReserveTermGenerationService(defaultPolicy, creation)

        then("the state is inspected after the creation call exits") {
            val outcome = service.ensureCurrentAndNext().single()
            outcome.result shouldBe ReserveTermGenerationResult.CONCURRENTLY_CREATED
            outcome.error shouldBe null
            verify(exactly = 1) { creation.inspectAfterIntegrityFailure(current) }
        }
    }

    given("an unexplained integrity failure") {
        val current = descriptor(2027, ReserveTermType.WINTER)
        val defaultPolicy = mockk<ReserveTermDefaultPolicy>()
        val creation = mockk<ReserveTermCreationService>()
        every { defaultPolicy.currentAndNextDescriptors() } returns listOf(current)
        every { creation.createDefault(current) } throws DataIntegrityViolationException("unknown")
        every { creation.inspectAfterIntegrityFailure(current) } returns
            ReserveTermCreationDecision(ReserveTermGenerationResult.FAILED, "unexplained_integrity_failure")

        then("the original exception is preserved") {
            val outcome = ReserveTermGenerationService(defaultPolicy, creation).ensureCurrentAndNext().single()
            outcome.result shouldBe ReserveTermGenerationResult.FAILED
            (outcome.error is DataIntegrityViolationException) shouldBe true
        }
    }
})
