package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.core.reservation.database.ReserveTermType
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class ReserveTermDescriptor(
    val termYear: Int,
    val termType: ReserveTermType,
    val applyStartTime: LocalDateTime,
    val applyEndTime: LocalDateTime,
    val termStartTime: LocalDateTime,
    val termEndTime: LocalDateTime
)

@Component
class ReserveTermDefaultPolicy(
    private val clock: Clock
) {
    fun currentAndNextDescriptors(): List<ReserveTermDescriptor> {
        val current = descriptorFor(LocalDate.now(clock))
        return listOf(current, descriptorFor(current.termEndTime.toLocalDate()))
    }

    fun descriptorFor(date: LocalDate): ReserveTermDescriptor {
        val type = when (date.monthValue) {
            1, 2 -> ReserveTermType.WINTER
            in 3..6 -> ReserveTermType.FIRST_SEMESTER
            7, 8 -> ReserveTermType.SUMMER
            else -> ReserveTermType.SECOND_SEMESTER
        }
        return descriptor(date.year, type)
    }

    fun descriptor(termYear: Int, termType: ReserveTermType): ReserveTermDescriptor {
        val termStartDate: LocalDate
        val termEndDate: LocalDate
        val applyStartDate: LocalDate

        when (termType) {
            ReserveTermType.WINTER -> {
                termStartDate = LocalDate.of(termYear, 1, 1)
                termEndDate = LocalDate.of(termYear, 3, 1)
                applyStartDate = LocalDate.of(termYear - 1, 12, 1)
            }
            ReserveTermType.FIRST_SEMESTER -> {
                termStartDate = LocalDate.of(termYear, 3, 1)
                termEndDate = LocalDate.of(termYear, 7, 1)
                applyStartDate = LocalDate.of(termYear, 2, 1)
            }
            ReserveTermType.SUMMER -> {
                termStartDate = LocalDate.of(termYear, 7, 1)
                termEndDate = LocalDate.of(termYear, 9, 1)
                applyStartDate = LocalDate.of(termYear, 6, 1)
            }
            ReserveTermType.SECOND_SEMESTER -> {
                termStartDate = LocalDate.of(termYear, 9, 1)
                termEndDate = LocalDate.of(termYear + 1, 1, 1)
                applyStartDate = LocalDate.of(termYear, 8, 1)
            }
        }

        val termStartTime = termStartDate.atStartOfDay()
        return ReserveTermDescriptor(
            termYear = termYear,
            termType = termType,
            applyStartTime = adjustWeekend(applyStartDate.atTime(OPEN_TIME)),
            applyEndTime = termStartTime,
            termStartTime = termStartTime,
            termEndTime = termEndDate.atStartOfDay()
        )
    }

    private fun adjustWeekend(dateTime: LocalDateTime): LocalDateTime = when (dateTime.dayOfWeek) {
        DayOfWeek.SATURDAY -> dateTime.plusDays(2)
        DayOfWeek.SUNDAY -> dateTime.plusDays(1)
        else -> dateTime
    }

    companion object {
        private val OPEN_TIME: LocalTime = LocalTime.of(9, 0)
    }
}
