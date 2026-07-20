package com.wafflestudio.csereal.core.reservation.service

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

data class ReserveTermGenerationOutcome(
    val descriptor: ReserveTermDescriptor,
    val result: ReserveTermGenerationResult,
    val error: Throwable? = null
)

@Service
class ReserveTermGenerationService(
    private val reserveTermDefaultPolicy: ReserveTermDefaultPolicy,
    private val reserveTermCreationService: ReserveTermCreationService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun ensureCurrentAndNext(): List<ReserveTermGenerationOutcome> {
        return reserveTermDefaultPolicy.currentAndNextDescriptors().map(::ensureDefault)
    }

    private fun ensureDefault(descriptor: ReserveTermDescriptor): ReserveTermGenerationOutcome {
        return try {
            outcome(descriptor, reserveTermCreationService.createDefault(descriptor))
        } catch (exception: DataIntegrityViolationException) {
            // Inspection starts only after the failed create transaction has rolled back.
            val decision = try {
                reserveTermCreationService.inspectAfterIntegrityFailure(descriptor)
            } catch (inspectionException: Exception) {
                logFailure(descriptor, inspectionException.javaClass.simpleName, emptyList())
                return ReserveTermGenerationOutcome(descriptor, ReserveTermGenerationResult.FAILED, exception)
            }
            outcome(descriptor, decision, exception)
        } catch (exception: Exception) {
            logFailure(descriptor, exception.javaClass.simpleName, emptyList())
            ReserveTermGenerationOutcome(descriptor, ReserveTermGenerationResult.FAILED, exception)
        }
    }

    private fun outcome(
        descriptor: ReserveTermDescriptor,
        decision: ReserveTermCreationDecision,
        integrityException: DataIntegrityViolationException? = null
    ): ReserveTermGenerationOutcome {
        val failed = decision.result in setOf(
            ReserveTermGenerationResult.SKIPPED_INVALID_EXISTING,
            ReserveTermGenerationResult.FAILED_INVALID_STATE,
            ReserveTermGenerationResult.FAILED
        )
        if (failed) {
            logFailure(descriptor, decision.reason, decision.candidates)
        } else {
            logger.info(
                "event=reserve_term_generation termYear={} termType={} result={}",
                descriptor.termYear,
                descriptor.termType,
                decision.result
            )
        }
        return ReserveTermGenerationOutcome(
            descriptor,
            decision.result,
            integrityException?.takeIf { failed }
        )
    }

    private fun logFailure(
        descriptor: ReserveTermDescriptor,
        reason: String?,
        candidates: Collection<com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity>
    ) {
        logger.error(
            "event=reserve_term_generation_failed termYear={} termType={} reason={} candidateIds={} " +
                "actualCandidates={} action={}",
            descriptor.termYear,
            descriptor.termType,
            reason,
            candidates.map { it.id },
            candidates.map(ReserveTermCandidateEvidence::from),
            "preserved_create_only"
        )
    }
}
