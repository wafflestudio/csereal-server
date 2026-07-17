package com.wafflestudio.csereal.core.reservation.scheduler

import com.wafflestudio.csereal.core.reservation.service.ReserveTermGenerationService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.mock.env.MockEnvironment
import org.springframework.scheduling.annotation.Scheduled

class ReserveTermGenerationSchedulerTest : StringSpec({
    "cron and zone placeholders resolve and the scheduler delegates" {
        val generationService = mockk<ReserveTermGenerationService>()
        every { generationService.ensureCurrentAndNext() } returns emptyList()
        val scheduler = ReserveTermGenerationScheduler(generationService)
        val annotation = ReserveTermGenerationScheduler::class.java
            .getDeclaredMethod("ensureCurrentAndNextReserveTerms")
            .getAnnotation(Scheduled::class.java)
        val environment = MockEnvironment()
            .withProperty("csereal.reservation.reserve-term-generation.cron", "0 30 4 * * SAT")
            .withProperty("csereal.reservation.reserve-term-generation.zone", "Asia/Seoul")

        environment.resolvePlaceholders(annotation.cron) shouldBe "0 30 4 * * SAT"
        environment.resolvePlaceholders(annotation.zone) shouldBe "Asia/Seoul"

        scheduler.ensureCurrentAndNextReserveTerms()
        verify(exactly = 1) { generationService.ensureCurrentAndNext() }
    }
})
