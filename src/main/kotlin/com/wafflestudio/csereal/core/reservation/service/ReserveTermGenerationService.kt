package com.wafflestudio.csereal.core.reservation.service

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

data class ReserveTermGenerationOutcome(
    val descriptor: ReserveTermDescriptor,
    val result: ReserveTermReconciliationResult?,
    val error: Throwable?
)

@Service
class ReserveTermGenerationService(
    private val reserveTermPolicy: ReserveTermPolicy,
    private val reserveTermReconciliationService: ReserveTermReconciliationService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun ensureCurrentAndNext(): List<ReserveTermGenerationOutcome> {
        return reserveTermPolicy.currentAndNextDescriptors().map { descriptor ->
            try {
                val result = reserveTermReconciliationService.ensureTerm(descriptor)
                logger.info(
                    "event=reserve_term_reconciled termYear={} termType={} result={}",
                    descriptor.termYear,
                    descriptor.termType,
                    result
                )
                ReserveTermGenerationOutcome(descriptor, result, null)
            } catch (exception: DataIntegrityViolationException) {
                verifyConcurrentInsert(descriptor, exception)
            } catch (exception: InvalidReserveTermStateException) {
                logger.error(
                    "event=reserve_term_reconciliation_failed termYear={} termType={} reason={} candidateIds={}",
                    descriptor.termYear,
                    descriptor.termType,
                    exception.audit.reason,
                    exception.audit.candidates.map { it.id }
                )
                ReserveTermGenerationOutcome(descriptor, null, exception)
            } catch (exception: Exception) {
                logger.error(
                    "event=reserve_term_reconciliation_failed termYear={} termType={} reason={} candidateIds=[]",
                    descriptor.termYear,
                    descriptor.termType,
                    exception.javaClass.simpleName
                )
                ReserveTermGenerationOutcome(descriptor, null, exception)
            }
        }
    }

    private fun verifyConcurrentInsert(
        descriptor: ReserveTermDescriptor,
        originalException: DataIntegrityViolationException
    ): ReserveTermGenerationOutcome {
        return try {
            val result = reserveTermReconciliationService.verifyAfterConcurrentInsert(descriptor)
            logger.info(
                "event=reserve_term_reconciled termYear={} termType={} result={}",
                descriptor.termYear,
                descriptor.termType,
                result
            )
            ReserveTermGenerationOutcome(descriptor, result, null)
        } catch (verificationException: InvalidReserveTermStateException) {
            logger.error(
                "event=reserve_term_reconciliation_failed termYear={} termType={} reason={} candidateIds={}",
                descriptor.termYear,
                descriptor.termType,
                verificationException.audit.reason,
                verificationException.audit.candidates.map { it.id }
            )
            ReserveTermGenerationOutcome(descriptor, null, originalException)
        } catch (verificationException: Exception) {
            logger.error(
                "event=reserve_term_reconciliation_failed termYear={} termType={} " +
                    "reason={} candidateIds=[]",
                descriptor.termYear,
                descriptor.termType,
                verificationException.javaClass.simpleName
            )
            ReserveTermGenerationOutcome(descriptor, null, originalException)
        }
    }
}
