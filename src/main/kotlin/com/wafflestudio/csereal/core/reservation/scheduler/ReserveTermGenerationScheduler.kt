package com.wafflestudio.csereal.core.reservation.scheduler

import com.wafflestudio.csereal.core.reservation.service.ReserveTermGenerationService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ReserveTermGenerationScheduler(
    private val reserveTermGenerationService: ReserveTermGenerationService
) {
    @Scheduled(
        cron = "\${csereal.reservation.reserve-term-generation.cron:0 0 3 * * SAT}",
        zone = "\${csereal.reservation.reserve-term-generation.zone:Asia/Seoul}"
    )
    fun ensureCurrentAndNextReserveTerms() {
        reserveTermGenerationService.ensureCurrentAndNext()
    }
}
