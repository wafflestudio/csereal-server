package com.wafflestudio.csereal.core.reservation.service

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ReserveTermValidationServiceTest : StringSpec({
    val policy = ReserveTermPolicy(Clock.fixed(Instant.EPOCH, ZoneId.of("Asia/Seoul")))

    "point resolution distinguishes missing valid invalid and multiple" {
        val repository = mockk<ReserveTermRepository>()
        val service = ReserveTermValidationService(repository, policy)
        val requestStart = LocalDateTime.of(2027, 3, 20, 10, 0)
        val valid = term(1)
        val invalid = term(2, applyEnd = LocalDateTime.of(2027, 3, 2, 0, 0))

        every { repository.findContainingRequestStart(requestStart) } returnsMany listOf(
            emptyList(),
            listOf(valid),
            listOf(invalid),
            listOf(valid, invalid)
        )

        service.resolveTarget(requestStart) shouldBe ReserveTermResolution.Missing
        service.resolveTarget(requestStart) shouldBe ReserveTermResolution.Valid(valid)
        (service.resolveTarget(requestStart) as ReserveTermResolution.Invalid).reasons shouldBe
            listOf("application_overlaps_term")
        (service.resolveTarget(requestStart) as ReserveTermResolution.Multiple).candidates shouldBe
            listOf(valid, invalid)
        verify(exactly = 4) { repository.findContainingRequestStart(requestStart) }
    }

    "validated listing uses one query and hides complete overlap components" {
        val repository = mockk<ReserveTermRepository>()
        val first = term(11, termEnd = LocalDateTime.of(2027, 5, 1, 0, 0))
        val overlap = term(
            12,
            termStart = LocalDateTime.of(2027, 4, 1, 0, 0),
            termEnd = LocalDateTime.of(2027, 6, 1, 0, 0)
        )
        val touching = term(
            13,
            applyStart = LocalDateTime.of(2027, 5, 1, 9, 0),
            applyEnd = LocalDateTime.of(2027, 6, 1, 0, 0),
            termStart = LocalDateTime.of(2027, 6, 1, 0, 0),
            termEnd = LocalDateTime.of(2027, 7, 1, 0, 0)
        )
        every { repository.findAllByOrderByApplyStartTimeAscTermStartTimeAscIdAsc() } returns
            listOf(first, overlap, touching)

        ReserveTermValidationService(repository, policy).findAllValidated().map { it.id } shouldBe listOf(13L)
        verify(exactly = 1) { repository.findAllByOrderByApplyStartTimeAscTermStartTimeAscIdAsc() }
    }

    "invalid evidence contains schedule fields but no request PII" {
        val repository = mockk<ReserveTermRepository>()
        val invalid = term(41, termYear = 2027)
        every { repository.findContainingRequestStart(any()) } returns listOf(invalid)
        val appender = logAppender()

        try {
            ReserveTermValidationService(repository, policy).resolveTarget(invalid.termStartTime.plusDays(1))
            val message = appender.list.single().formattedMessage
            message.contains("candidateIds=[41]") shouldBe true
            message.contains("partial_metadata") shouldBe true
            message.contains("applyStartTime=") shouldBe true
            message.contains("contact") shouldBe false
            message.contains("title") shouldBe false
        } finally {
            appender.stop()
        }
    }
}) {
    companion object {
        private fun term(
            id: Long,
            applyStart: LocalDateTime = LocalDateTime.of(2027, 2, 1, 9, 0),
            applyEnd: LocalDateTime = LocalDateTime.of(2027, 3, 1, 0, 0),
            termStart: LocalDateTime = LocalDateTime.of(2027, 3, 1, 0, 0),
            termEnd: LocalDateTime = LocalDateTime.of(2027, 7, 1, 0, 0),
            termYear: Int? = null
        ) = ReserveTermEntity(applyStart, applyEnd, termStart, termEnd, termYear, null).also { setId(it, id) }

        private fun setId(entity: BaseTimeEntity, id: Long) {
            BaseTimeEntity::class.java.getDeclaredField("id").apply {
                isAccessible = true
                setLong(entity, id)
            }
        }

        private fun logAppender(): ListAppender<ILoggingEvent> {
            val appender = ListAppender<ILoggingEvent>()
            appender.start()
            (LoggerFactory.getLogger(ReserveTermValidationService::class.java) as Logger).addAppender(appender)
            return appender
        }
    }
}
