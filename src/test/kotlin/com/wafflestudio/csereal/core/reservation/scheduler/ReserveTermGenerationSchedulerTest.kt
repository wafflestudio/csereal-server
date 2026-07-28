package com.wafflestudio.csereal.core.reservation.scheduler

import com.wafflestudio.csereal.core.reservation.service.ReserveTermGenerationService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.scheduling.annotation.Scheduled

class ReserveTermGenerationSchedulerTest : StringSpec({
    "scheduler is fixed to Saturday 03:00 Asia/Seoul and delegates" {
        val generationService = mockk<ReserveTermGenerationService>()
        every { generationService.ensureCurrentAndNext() } returns emptyList()
        val scheduler = ReserveTermGenerationScheduler(generationService)
        val annotation = ReserveTermGenerationScheduler::class.java
            .getDeclaredMethod("ensureCurrentAndNextReserveTerms")
            .getAnnotation(Scheduled::class.java)

        annotation.cron shouldBe "0 0 3 * * SAT"
        annotation.zone shouldBe "Asia/Seoul"
        scheduler.ensureCurrentAndNextReserveTerms()
        verify(exactly = 1) { generationService.ensureCurrentAndNext() }
    }
})
