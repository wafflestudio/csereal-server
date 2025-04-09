package com.wafflestudio.csereal.core.notice.scheduler

import com.wafflestudio.csereal.core.notice.database.NoticeRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

@Component
class NoticeScheduler(
    private val noticeRepository: NoticeRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Scheduled task to update the status of notices whose pin or importance period has expired.
     * Runs every day at 1:00 AM KST.
     */
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    @Transactional
    fun updateNoticeExpirationStatus() {
        val currentDate = LocalDate.now(ZoneId.of("Asia/Seoul"))
        // This task updates notices expired *before* the start of today (currentDate KST).
        logger.info("[updateNoticeExpirationStatus] Running scheduled task to update notices expired before: {}", currentDate)

        try {
            val updatedPinnedCount = noticeRepository.updateExpiredPinnedStatus(currentDate)
            logger.info("[updateNoticeExpirationStatus] Unpinned {} notices.", updatedPinnedCount)

            val updatedImportantCount = noticeRepository.updateExpiredImportantStatus(currentDate)
            logger.info("[updateNoticeExpirationStatus] Marked {} notices as not important.", updatedImportantCount)

        } catch (e: Exception) {
            logger.error("[updateNoticeExpirationStatus] Error occurred during notice expiration update for date {}.", currentDate, e)
        }
         logger.info("[updateNoticeExpirationStatus] Finished scheduled task for notices expired before: {}", currentDate)
    }
} 
