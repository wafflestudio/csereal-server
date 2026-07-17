package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermType
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

data class ReserveTermDescriptor(
    val termYear: Int,
    val termType: ReserveTermType,
    val applyStartTime: LocalDateTime,
    val applyEndTime: LocalDateTime,
    val termStartTime: LocalDateTime,
    val termEndTime: LocalDateTime
)

data class ReserveTermAudit(
    val descriptor: ReserveTermDescriptor,
    val candidates: List<ReserveTermEntity>,
    val validEntity: ReserveTermEntity?,
    val reason: String?
)

@Component
class ReserveTermPolicy(
    val clock: Clock
) {
    fun now(): LocalDateTime = LocalDateTime.now(clock)

    fun adHocOpenTime(reservationStart: LocalDateTime): LocalDateTime {
        return adjustWeekend(reservationStart.toLocalDate().minusWeeks(2).atTime(OPEN_TIME))
    }

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
        val termEndTime = termEndDate.atStartOfDay()
        return ReserveTermDescriptor(
            termYear = termYear,
            termType = termType,
            applyStartTime = adjustWeekend(applyStartDate.atTime(OPEN_TIME)),
            applyEndTime = termEndTime,
            termStartTime = termStartTime,
            termEndTime = termEndTime
        )
    }

    fun descriptorFor(entity: ReserveTermEntity): ReserveTermDescriptor? {
        val year = entity.termYear
        val type = entity.termType
        if ((year == null) != (type == null)) return null
        return if (year != null && type != null) {
            descriptor(year, type)
        } else {
            descriptorFor(entity.termStartTime.toLocalDate())
        }
    }

    fun audit(
        descriptor: ReserveTermDescriptor,
        keyedRows: Collection<ReserveTermEntity>,
        overlappingRows: Collection<ReserveTermEntity>
    ): ReserveTermAudit {
        val candidates = (keyedRows + overlappingRows).distinct()
        if (candidates.isEmpty()) {
            return ReserveTermAudit(descriptor, emptyList(), null, "missing")
        }
        if (candidates.size != 1) {
            return ReserveTermAudit(descriptor, candidates, null, "multiple_or_competing_rows")
        }

        val candidate = candidates.single()
        val reason = mismatchReason(candidate, descriptor)
        return if (reason == null) {
            ReserveTermAudit(descriptor, candidates, candidate, null)
        } else {
            ReserveTermAudit(descriptor, candidates, null, reason)
        }
    }

    fun mismatchReason(entity: ReserveTermEntity, descriptor: ReserveTermDescriptor): String? {
        val metadataMatches =
            (entity.termYear == null && entity.termType == null) ||
                (entity.termYear == descriptor.termYear && entity.termType == descriptor.termType)
        return when {
            !metadataMatches -> "metadata_mismatch"
            entity.applyStartTime != descriptor.applyStartTime -> "apply_start_mismatch"
            entity.applyEndTime != descriptor.applyEndTime -> "apply_end_mismatch"
            entity.termStartTime != descriptor.termStartTime -> "term_start_mismatch"
            entity.termEndTime != descriptor.termEndTime -> "term_end_mismatch"
            else -> null
        }
    }

    fun maxOccurrences(firstEndTime: LocalDateTime, boundaryEndTime: LocalDateTime): Long {
        if (firstEndTime.isAfter(boundaryEndTime)) return 0
        return ChronoUnit.WEEKS.between(firstEndTime, boundaryEndTime) + 1
    }

    fun staffMaxOccurrences(referenceYear: Int = LocalDate.now(clock).year): Long {
        return ReserveTermType.entries.maxOf { type ->
            val descriptor = descriptor(referenceYear, type)
            maxOccurrences(descriptor.termStartTime, descriptor.termEndTime)
        }
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
