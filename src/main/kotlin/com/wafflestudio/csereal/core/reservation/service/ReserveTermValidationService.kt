package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermRepository
import com.wafflestudio.csereal.core.reservation.database.ReserveTermType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

sealed interface ReserveTermResolution {
    data object Missing : ReserveTermResolution
    data class Valid(val term: ReserveTermEntity) : ReserveTermResolution
    data class Invalid(val candidate: ReserveTermEntity, val reasons: List<String>) : ReserveTermResolution
    data class Multiple(val candidates: List<ReserveTermEntity>) : ReserveTermResolution
}

internal data class ReserveTermCandidateEvidence(
    val id: Long,
    val termYear: Int?,
    val termType: ReserveTermType?,
    val applyStartTime: LocalDateTime,
    val applyEndTime: LocalDateTime,
    val termStartTime: LocalDateTime,
    val termEndTime: LocalDateTime
) {
    companion object {
        fun from(entity: ReserveTermEntity) = ReserveTermCandidateEvidence(
            entity.id,
            entity.termYear,
            entity.termType,
            entity.applyStartTime,
            entity.applyEndTime,
            entity.termStartTime,
            entity.termEndTime
        )
    }
}

@Service
class ReserveTermValidationService(
    private val reserveTermRepository: ReserveTermRepository,
    private val reserveTermPolicy: ReserveTermPolicy
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun resolveTarget(requestStart: LocalDateTime): ReserveTermResolution {
        val candidates = reserveTermRepository.findContainingRequestStart(requestStart)
        return when {
            candidates.isEmpty() -> ReserveTermResolution.Missing
            candidates.size > 1 -> ReserveTermResolution.Multiple(candidates).also {
                logInvalid("multiple_candidates", candidates)
            }
            else -> {
                val candidate = candidates.single()
                val reasons = reserveTermPolicy.invalidReasons(candidate)
                if (reasons.isEmpty()) {
                    ReserveTermResolution.Valid(candidate)
                } else {
                    ReserveTermResolution.Invalid(candidate, reasons).also {
                        logInvalid(reasons.joinToString(","), listOf(candidate))
                    }
                }
            }
        }
    }

    @Transactional(readOnly = true)
    fun findAllValidated(): List<ReserveTermEntity> {
        val rows = reserveTermRepository.findAllByOrderByApplyStartTimeAscTermStartTimeAscIdAsc()
        val structurallyValid = rows.filter { row ->
            val reasons = reserveTermPolicy.invalidReasons(row)
            if (reasons.isNotEmpty()) logInvalid(reasons.joinToString(","), listOf(row))
            reasons.isEmpty()
        }

        // Every member of an overlapping component is hidden to match point-resolution ambiguity.
        val overlaps = buildList {
            structurallyValid.forEachIndexed { index, left ->
                structurallyValid.drop(index + 1).forEach { right ->
                    if (left.termStartTime < right.termEndTime && left.termEndTime > right.termStartTime) {
                        add(left to right)
                    }
                }
            }
        }
        overlaps.forEach { (left, right) ->
            logInvalid("overlapping_term_window", listOf(left, right))
        }
        val overlappingIds = overlaps.flatMap { (left, right) -> listOf(left.id, right.id) }.toSet()
        return structurallyValid.filterNot { it.id in overlappingIds }
    }

    internal fun logInvalid(reason: String, candidates: Collection<ReserveTermEntity>) {
        logger.error(
            "event=reserve_term_invalid reason={} candidateIds={} actualCandidates={} action={}",
            reason,
            candidates.map { it.id },
            candidates.map(ReserveTermCandidateEvidence::from),
            "preserved_fail_closed"
        )
    }
}
