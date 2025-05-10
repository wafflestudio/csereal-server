package com.wafflestudio.csereal.core.seminar.scheduler

import com.wafflestudio.csereal.core.seminar.database.SeminarRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

@Component
class SeminarScheduler(
    private val seminarRepository: SeminarRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Scheduled task to update the status of seminars whose importance period has expired.
     * Runs every day at 1:05 AM KST.
     */
    @Scheduled(cron = "0 5 1 * * *", zone = "Asia/Seoul")
    @Transactional
    fun updateSeminarExpirationStatus() {
        val currentDate = LocalDate.now(ZoneId.of("Asia/Seoul"))
        logger.info(
            "[updateSeminarExpirationStatus] Running scheduled task to update seminars expired before: {}",
            currentDate
        )

        try {
            val updatedCount = seminarRepository.updateExpiredImportantStatus(currentDate)
            logger.info("[updateSeminarExpirationStatus] Marked {} seminars as not important.", updatedCount)
        } catch (e: Exception) {
            logger.error(
                "[updateSeminarExpirationStatus] Error occurred during seminar expiration update for date {}.",
                currentDate,
                e
            )
        }
        logger.info(
            "[updateSeminarExpirationStatus] Finished scheduled task for seminars expired before: {}",
            currentDate
        )
    }
} 
