package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermRepository
import com.wafflestudio.csereal.core.reservation.database.ReserveTermType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ReserveTermCreationServiceTest : StringSpec({
    val policy = ReserveTermPolicy(Clock.fixed(Instant.EPOCH, ZoneId.of("Asia/Seoul")))
    val descriptor = descriptor()

    "an existing valid keyed row is preserved even when its times are custom" {
        val repository = mockk<ReserveTermRepository>(relaxed = true)
        val custom = descriptor.toEntity(applyStart = descriptor.applyStartTime.plusDays(2))
        every { repository.findByTermYearAndTermTypeOrderByIdAsc(descriptor.termYear, descriptor.termType) } returns
            listOf(custom)

        ReserveTermCreationService(repository, policy).createDefault(descriptor).result shouldBe
            ReserveTermGenerationResult.EXISTING
        verify(exactly = 0) { repository.findByTimeOverlap(any(), any()) }
        verify(exactly = 0) { repository.saveAndFlush(any()) }
    }

    "an invalid existing keyed row is preserved without repair" {
        val repository = mockk<ReserveTermRepository>(relaxed = true)
        val invalid = descriptor.toEntity(applyEnd = descriptor.applyStartTime)
        every { repository.findByTermYearAndTermTypeOrderByIdAsc(descriptor.termYear, descriptor.termType) } returns
            listOf(invalid)

        ReserveTermCreationService(repository, policy).createDefault(descriptor).result shouldBe
            ReserveTermGenerationResult.SKIPPED_INVALID_EXISTING
        verify(exactly = 0) { repository.saveAndFlush(any()) }
    }

    "an unlabelled custom overlap blocks default insertion but a touching row does not" {
        val repository = mockk<ReserveTermRepository>(relaxed = true)
        val custom = descriptor.toEntity(termYear = null, termType = null)
        every { repository.findByTermYearAndTermTypeOrderByIdAsc(descriptor.termYear, descriptor.termType) } returns
            emptyList()
        every { repository.findByTimeOverlap(descriptor.termStartTime, descriptor.termEndTime) } returnsMany
            listOf(listOf(custom), emptyList())
        every { repository.saveAndFlush(any()) } answers { firstArg() }
        val service = ReserveTermCreationService(repository, policy)

        service.createDefault(descriptor).result shouldBe ReserveTermGenerationResult.SKIPPED_CUSTOM_OVERLAP
        service.createDefault(descriptor).result shouldBe ReserveTermGenerationResult.CREATED
        verify(exactly = 1) { repository.saveAndFlush(any()) }
    }

    "integrity-failure inspection classifies the actual post-rollback state" {
        val repository = mockk<ReserveTermRepository>(relaxed = true)
        val valid = descriptor.toEntity()
        every { repository.findByTermYearAndTermTypeOrderByIdAsc(descriptor.termYear, descriptor.termType) } returnsMany
            listOf(listOf(valid), emptyList(), emptyList())
        every { repository.findByTimeOverlap(descriptor.termStartTime, descriptor.termEndTime) } returnsMany
            listOf(listOf(valid), emptyList())
        val service = ReserveTermCreationService(repository, policy)

        service.inspectAfterIntegrityFailure(descriptor).result shouldBe
            ReserveTermGenerationResult.CONCURRENTLY_CREATED
        service.inspectAfterIntegrityFailure(descriptor).result shouldBe
            ReserveTermGenerationResult.SKIPPED_CUSTOM_OVERLAP
        service.inspectAfterIntegrityFailure(descriptor).result shouldBe ReserveTermGenerationResult.FAILED
    }
}) {
    companion object {
        private fun descriptor() = ReserveTermDescriptor(
            2027,
            ReserveTermType.FIRST_SEMESTER,
            LocalDateTime.of(2027, 2, 1, 9, 0),
            LocalDateTime.of(2027, 3, 1, 0, 0),
            LocalDateTime.of(2027, 3, 1, 0, 0),
            LocalDateTime.of(2027, 7, 1, 0, 0)
        )

        private fun ReserveTermDescriptor.toEntity(
            applyStart: LocalDateTime = applyStartTime,
            applyEnd: LocalDateTime = applyEndTime,
            termYear: Int? = this.termYear,
            termType: ReserveTermType? = this.termType
        ) = ReserveTermEntity(
            applyStart,
            applyEnd,
            termStartTime,
            termEndTime,
            termYear,
            termType
        )
    }
}
