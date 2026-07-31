package com.wafflestudio.csereal.core.reservation.dto

import com.wafflestudio.csereal.core.reservation.database.ReserveTermType
import com.wafflestudio.csereal.core.reservation.service.ReserveTermGenerationOutcome
import com.wafflestudio.csereal.core.reservation.service.ReserveTermGenerationResult
import java.time.LocalDateTime

data class CreateCustomReserveTermRequest(
    val applyStartTime: LocalDateTime,
    val applyEndTime: LocalDateTime,
    val termStartTime: LocalDateTime,
    val termEndTime: LocalDateTime
)

data class ReserveTermGenerationOutcomeDto(
    val termYear: Int,
    val termType: ReserveTermType,
    val result: ReserveTermGenerationResult,
    val reason: String?
) {
    companion object {
        fun of(outcome: ReserveTermGenerationOutcome): ReserveTermGenerationOutcomeDto {
            return ReserveTermGenerationOutcomeDto(
                termYear = outcome.descriptor.termYear,
                termType = outcome.descriptor.termType,
                result = outcome.result,
                reason = when (outcome.result) {
                    ReserveTermGenerationResult.CREATED,
                    ReserveTermGenerationResult.EXISTING,
                    ReserveTermGenerationResult.CONCURRENTLY_CREATED -> null
                    ReserveTermGenerationResult.SKIPPED_INVALID_EXISTING -> "invalid_existing_term"
                    ReserveTermGenerationResult.SKIPPED_CUSTOM_OVERLAP -> "overlapping_custom_term"
                    ReserveTermGenerationResult.FAILED_INVALID_STATE -> "invalid_existing_state"
                    ReserveTermGenerationResult.FAILED -> "generation_failed"
                }
            )
        }
    }
}
