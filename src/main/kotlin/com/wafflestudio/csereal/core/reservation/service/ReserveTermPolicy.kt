package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

enum class ReserveTermPhase {
    BEFORE_APPLICATION,
    REGULAR_APPLICATION,
    GAP,
    TERM_ACTIVE
}

@Component
class ReserveTermPolicy(
    val clock: Clock
) {
    fun now(): LocalDateTime = LocalDateTime.now(clock)

    fun invalidReasons(entity: ReserveTermEntity): List<String> = buildList {
        if (!entity.applyStartTime.isBefore(entity.applyEndTime)) add("invalid_application_window")
        if (entity.applyEndTime.isAfter(entity.termStartTime)) add("application_overlaps_term")
        if (!entity.termStartTime.isBefore(entity.termEndTime)) add("invalid_term_window")
        if ((entity.termYear == null) != (entity.termType == null)) add("partial_metadata")
    }

    fun phase(entity: ReserveTermEntity, now: LocalDateTime = now()): ReserveTermPhase = when {
        now.isBefore(entity.applyStartTime) -> ReserveTermPhase.BEFORE_APPLICATION
        now.isBefore(entity.applyEndTime) -> ReserveTermPhase.REGULAR_APPLICATION
        now.isBefore(entity.termStartTime) -> ReserveTermPhase.GAP
        else -> ReserveTermPhase.TERM_ACTIVE
    }

    fun adHocOpenTime(reservationStart: LocalDateTime): LocalDateTime {
        return adjustWeekend(reservationStart.toLocalDate().minusWeeks(2).atTime(OPEN_TIME))
    }

    fun activeTermAdHocOpenTime(term: ReserveTermEntity, reservationStart: LocalDateTime): LocalDateTime {
        return maxOf(term.termStartTime, adHocOpenTime(reservationStart))
    }

    fun maxOccurrences(firstEndTime: LocalDateTime, boundaryEndTime: LocalDateTime): Long {
        if (firstEndTime.isAfter(boundaryEndTime)) return 0
        return ChronoUnit.WEEKS.between(firstEndTime, boundaryEndTime) + 1
    }

    private fun adjustWeekend(dateTime: LocalDateTime): LocalDateTime {
        return when (dateTime.dayOfWeek) {
            DayOfWeek.SATURDAY -> dateTime.plusDays(2)
            DayOfWeek.SUNDAY -> dateTime.plusDays(1)
            else -> dateTime
        }
    }

    companion object {
        private val OPEN_TIME: LocalTime = LocalTime.of(9, 0)
    }
}
