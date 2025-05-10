package com.wafflestudio.csereal.core.news.scheduler

import com.wafflestudio.csereal.core.news.database.NewsRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

@Component
class NewsScheduler(
    private val newsRepository: NewsRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Scheduled task to update the status of news items whose importance period has expired.
     * Runs every day at 1:00 AM KST.
     */
    @Scheduled(cron = "0 10 1 * * *", zone = "Asia/Seoul")
    @Transactional
    fun updateNewsExpirationStatus() {
        val currentDate = LocalDate.now(ZoneId.of("Asia/Seoul"))
        logger.info(
            "[updateNewsExpirationStatus] Running scheduled task to update news expired before: {}",
            currentDate
        )

        try {
            val updatedCount = newsRepository.updateExpiredImportantStatus(currentDate)
            logger.info("[updateNewsExpirationStatus] Marked {} news items as not important.", updatedCount)
        } catch (e: Exception) {
            logger.error(
                "[updateNewsExpirationStatus] Error occurred during news expiration update for date {}.",
                currentDate,
                e
            )
        }
        logger.info(
            "[updateNewsExpirationStatus] Finished scheduled task for news expired before: {}",
            currentDate
        )
    }
}
