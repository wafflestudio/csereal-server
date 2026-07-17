package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
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
import java.time.ZoneId

class ReserveTermValidationServiceTest : StringSpec({
    "findAllValidated uses one bounded repository query regardless of term count" {
        val repository = mockk<ReserveTermRepository>(relaxed = true)
        val policy = ReserveTermPolicy(
            Clock.fixed(Instant.parse("2027-04-01T00:00:00Z"), ZoneId.of("Asia/Seoul"))
        )
        val rows = listOf(
            entity(policy.descriptor(2027, ReserveTermType.FIRST_SEMESTER), 1),
            entity(policy.descriptor(2027, ReserveTermType.SUMMER), 2),
            entity(policy.descriptor(2027, ReserveTermType.SECOND_SEMESTER), 3),
            entity(policy.descriptor(2028, ReserveTermType.WINTER), 4)
        )
        every { repository.findAllByOrderByApplyStartTimeAscTermStartTimeAscIdAsc() } returns rows
        val service = ReserveTermValidationService(repository, policy)

        service.findAllValidated().map { it.id } shouldBe listOf(1L, 2L, 3L, 4L)
        verify(exactly = 1) { repository.findAllByOrderByApplyStartTimeAscTermStartTimeAscIdAsc() }
        verify(exactly = 0) { repository.findByTermYearAndTermType(any(), any()) }
        verify(exactly = 0) { repository.findByTimeOverlap(any(), any()) }
    }
}) {
    companion object {
        private fun entity(descriptor: ReserveTermDescriptor, id: Long): ReserveTermEntity {
            return ReserveTermEntity(
                descriptor.applyStartTime,
                descriptor.applyEndTime,
                descriptor.termStartTime,
                descriptor.termEndTime,
                descriptor.termYear,
                descriptor.termType
            ).also { entity ->
                BaseTimeEntity::class.java.getDeclaredField("id").apply {
                    isAccessible = true
                    setLong(entity, id)
                }
            }
        }
    }
}
