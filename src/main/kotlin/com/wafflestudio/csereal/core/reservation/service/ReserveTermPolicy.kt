package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
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
    fun now(): LocalDateTime = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC)

    fun invalidReasons(entity: ReserveTermEntity): List<String> = buildList {
        if (!entity.applyStartTime.isBefore(entity.applyEndTime)) add("invalid_application_window")
        if (entity.applyEndTime.isAfter(entity.termEndTime)) add("application_ends_after_term")
        if (!entity.termStartTime.isBefore(entity.termEndTime)) add("invalid_term_window")
        if ((entity.termYear == null) != (entity.termType == null)) add("partial_metadata")
    }

    fun phase(entity: ReserveTermEntity, now: LocalDateTime = now()): ReserveTermPhase = when {
        !now.isBefore(entity.applyStartTime) && now.isBefore(entity.applyEndTime) ->
            ReserveTermPhase.REGULAR_APPLICATION
        !now.isBefore(entity.termStartTime) -> ReserveTermPhase.TERM_ACTIVE
        now.isBefore(entity.applyStartTime) -> ReserveTermPhase.BEFORE_APPLICATION
        else -> ReserveTermPhase.GAP
    }

    fun oneTimeReservationOpenTime(reservationStart: LocalDateTime): LocalDateTime {
        val reservationDate = reservationStart.toInstant(ZoneOffset.UTC).atZone(SEOUL_ZONE).toLocalDate()
        return adjustWeekend(reservationDate.minusWeeks(2).atTime(OPEN_TIME))
            .atZone(SEOUL_ZONE)
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime()
    }

    fun activeTermOneTimeReservationOpenTime(term: ReserveTermEntity, reservationStart: LocalDateTime): LocalDateTime {
        return maxOf(term.termStartTime, oneTimeReservationOpenTime(reservationStart))
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
        private val SEOUL_ZONE: ZoneId = ZoneId.of("Asia/Seoul")
        private val OPEN_TIME: LocalTime = LocalTime.of(9, 0)
    }
}
