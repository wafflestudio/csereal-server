package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

enum class ReserveTermGenerationResult {
    CREATED,
    EXISTING,
    SKIPPED_INVALID_EXISTING,
    SKIPPED_CUSTOM_OVERLAP,
    CONCURRENTLY_CREATED,
    FAILED_INVALID_STATE,
    FAILED
}

data class ReserveTermCreationDecision(
    val result: ReserveTermGenerationResult,
    val reason: String? = null,
    val candidates: List<ReserveTermEntity> = emptyList()
)

@Service
class ReserveTermCreationService(
    private val reserveTermRepository: ReserveTermRepository,
    private val reserveTermPolicy: ReserveTermPolicy
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun createDefault(descriptor: ReserveTermDescriptor): ReserveTermCreationDecision {
        val keyedRows = reserveTermRepository.findByTermYearAndTermTypeOrderByIdAsc(
            descriptor.termYear,
            descriptor.termType
        )
        classifyKeyedRows(keyedRows, concurrent = false)?.let { return it }

        val overlappingRows = reserveTermRepository.findByTimeOverlap(
            descriptor.termStartTime,
            descriptor.termEndTime
        )
        if (overlappingRows.isNotEmpty()) {
            return ReserveTermCreationDecision(
                ReserveTermGenerationResult.SKIPPED_CUSTOM_OVERLAP,
                "custom_overlap",
                overlappingRows
            )
        }

        reserveTermRepository.saveAndFlush(descriptor.toEntity())
        return ReserveTermCreationDecision(ReserveTermGenerationResult.CREATED)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    fun inspectAfterIntegrityFailure(descriptor: ReserveTermDescriptor): ReserveTermCreationDecision {
        val keyedRows = reserveTermRepository.findByTermYearAndTermTypeOrderByIdAsc(
            descriptor.termYear,
            descriptor.termType
        )
        classifyKeyedRows(keyedRows, concurrent = true)?.let { return it }

        val overlappingRows = reserveTermRepository.findByTimeOverlap(
            descriptor.termStartTime,
            descriptor.termEndTime
        )
        if (overlappingRows.isNotEmpty()) {
            return ReserveTermCreationDecision(
                ReserveTermGenerationResult.SKIPPED_CUSTOM_OVERLAP,
                "custom_overlap_after_integrity_failure",
                overlappingRows
            )
        }
        return ReserveTermCreationDecision(
            ReserveTermGenerationResult.FAILED,
            "unexplained_integrity_failure"
        )
    }

    private fun classifyKeyedRows(
        keyedRows: List<ReserveTermEntity>,
        concurrent: Boolean
    ): ReserveTermCreationDecision? {
        if (keyedRows.isEmpty()) return null
        if (keyedRows.size > 1) {
            return ReserveTermCreationDecision(
                ReserveTermGenerationResult.FAILED_INVALID_STATE,
                "multiple_keyed_rows",
                keyedRows
            )
        }

        val row = keyedRows.single()
        val reasons = reserveTermPolicy.invalidReasons(row)
        if (reasons.isNotEmpty()) {
            return ReserveTermCreationDecision(
                ReserveTermGenerationResult.SKIPPED_INVALID_EXISTING,
                reasons.joinToString(","),
                keyedRows
            )
        }
        return ReserveTermCreationDecision(
            if (concurrent) ReserveTermGenerationResult.CONCURRENTLY_CREATED else ReserveTermGenerationResult.EXISTING,
            candidates = keyedRows
        )
    }

    private fun ReserveTermDescriptor.toEntity() = ReserveTermEntity(
        applyStartTime = applyStartTime,
        applyEndTime = applyEndTime,
        termStartTime = termStartTime,
        termEndTime = termEndTime,
        termYear = termYear,
        termType = termType
    )
}
