package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

enum class ReserveTermReconciliationResult {
    CREATED,
    EXISTING,
    METADATA_ATTACHED,
    CONCURRENTLY_CREATED
}

class InvalidReserveTermStateException(
    val audit: ReserveTermAudit
) : RuntimeException("Invalid reserve term state: ${audit.reason}")

@Service
class ReserveTermReconciliationService(
    private val reserveTermRepository: ReserveTermRepository,
    private val reserveTermValidationService: ReserveTermValidationService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun ensureTerm(descriptor: ReserveTermDescriptor): ReserveTermReconciliationResult {
        val audit = reserveTermValidationService.audit(descriptor)
        val validEntity = audit.validEntity

        if (validEntity != null) {
            if (validEntity.termYear == null && validEntity.termType == null) {
                validEntity.assignCanonicalMetadata(descriptor.termYear, descriptor.termType)
                reserveTermRepository.saveAndFlush(validEntity)
                return ReserveTermReconciliationResult.METADATA_ATTACHED
            }
            return ReserveTermReconciliationResult.EXISTING
        }

        if (audit.reason != "missing") {
            reserveTermValidationService.logInvalid(audit)
            throw InvalidReserveTermStateException(audit)
        }

        reserveTermRepository.saveAndFlush(
            ReserveTermEntity(
                applyStartTime = descriptor.applyStartTime,
                applyEndTime = descriptor.applyEndTime,
                termStartTime = descriptor.termStartTime,
                termEndTime = descriptor.termEndTime,
                termYear = descriptor.termYear,
                termType = descriptor.termType
            )
        )
        return ReserveTermReconciliationResult.CREATED
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    fun verifyAfterConcurrentInsert(descriptor: ReserveTermDescriptor): ReserveTermReconciliationResult {
        val audit = reserveTermValidationService.audit(descriptor)
        if (audit.validEntity != null && audit.validEntity.termYear == descriptor.termYear) {
            return ReserveTermReconciliationResult.CONCURRENTLY_CREATED
        }
        val evidence = ReserveTermAuditEvidence.from(descriptor, audit.candidates)
        logger.error(
            "event=reserve_term_concurrent_insert_invalid termYear={} termType={} reason={} candidateIds={} " +
                "expected={} actualCandidates={} action={}",
            descriptor.termYear,
            descriptor.termType,
            audit.reason,
            audit.candidates.map { it.id },
            evidence.expected,
            evidence.actualCandidates,
            evidence.action
        )
        throw InvalidReserveTermStateException(audit)
    }
}
