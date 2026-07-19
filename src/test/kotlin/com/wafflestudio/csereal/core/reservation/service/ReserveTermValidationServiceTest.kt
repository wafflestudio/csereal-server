package com.wafflestudio.csereal.core.reservation.service

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermRepository
import com.wafflestudio.csereal.core.reservation.database.ReserveTermType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.slf4j.LoggerFactory
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

    "partial metadata log exposes expected and actual values with the fail-closed action" {
        val repository = mockk<ReserveTermRepository>()
        val policy = ReserveTermPolicy(
            Clock.fixed(Instant.parse("2027-04-01T00:00:00Z"), ZoneId.of("Asia/Seoul"))
        )
        val descriptor = policy.descriptor(2027, ReserveTermType.FIRST_SEMESTER)
        val partial = ReserveTermEntity(
            descriptor.applyStartTime,
            descriptor.applyEndTime,
            descriptor.termStartTime,
            descriptor.termEndTime,
            descriptor.termYear,
            null
        ).also { setId(it, 41) }
        every { repository.findAllByOrderByApplyStartTimeAscTermStartTimeAscIdAsc() } returns listOf(partial)
        val appender = logAppender()

        try {
            ReserveTermValidationService(repository, policy).findAllValidated() shouldBe emptyList()
            val message = appender.list.single().formattedMessage
            message.contains("reason=partial_metadata") shouldBe true
            message.contains("candidateIds=[41]") shouldBe true
            message.contains("expected=ReserveTermDescriptor(termYear=2027") shouldBe true
            message.contains(
                "actualCandidates=[ReserveTermCandidateEvidence(id=41, termYear=2027, termType=null"
            ) shouldBe true
            message.contains("action=preserved_fail_closed") shouldBe true
        } finally {
            appender.stop()
        }
    }

    "conflicting audit log exposes every candidate value with the fail-closed action" {
        val repository = mockk<ReserveTermRepository>()
        val policy = ReserveTermPolicy(
            Clock.fixed(Instant.parse("2027-04-01T00:00:00Z"), ZoneId.of("Asia/Seoul"))
        )
        val descriptor = policy.descriptor(2027, ReserveTermType.FIRST_SEMESTER)
        val first = entity(descriptor, 51)
        val second = entity(descriptor, 52)
        every { repository.findByTermYearAndTermType(descriptor.termYear, descriptor.termType) } returns
            listOf(first, second)
        every { repository.findByTimeOverlap(descriptor.termStartTime, descriptor.termEndTime) } returns
            listOf(first, second)
        val appender = logAppender()

        try {
            ReserveTermValidationService(repository, policy).apply {
                val audit = audit(descriptor)
                logInvalid(audit)
            }
            val message = appender.list.single().formattedMessage
            message.contains("reason=multiple_or_competing_rows") shouldBe true
            message.contains("candidateIds=[51, 52]") shouldBe true
            message.contains("ReserveTermCandidateEvidence(id=51") shouldBe true
            message.contains("ReserveTermCandidateEvidence(id=52") shouldBe true
            message.contains("action=preserved_fail_closed") shouldBe true
        } finally {
            appender.stop()
        }
    }
}) {
    companion object {
        private fun logAppender(): ListAppender<ILoggingEvent> {
            val appender = ListAppender<ILoggingEvent>()
            appender.start()
            (LoggerFactory.getLogger(ReserveTermValidationService::class.java) as Logger).addAppender(appender)
            return appender
        }

        private fun entity(descriptor: ReserveTermDescriptor, id: Long): ReserveTermEntity {
            return ReserveTermEntity(
                descriptor.applyStartTime,
                descriptor.applyEndTime,
                descriptor.termStartTime,
                descriptor.termEndTime,
                descriptor.termYear,
                descriptor.termType
            ).also { setId(it, id) }
        }

        private fun setId(entity: BaseTimeEntity, id: Long) {
            BaseTimeEntity::class.java.getDeclaredField("id").apply {
                isAccessible = true
                setLong(entity, id)
            }
        }
    }
}
