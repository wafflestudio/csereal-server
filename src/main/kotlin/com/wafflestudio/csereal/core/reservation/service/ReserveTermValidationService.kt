package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

internal data class ReserveTermCandidateEvidence(
    val id: Long,
    val termYear: Int?,
    val termType: com.wafflestudio.csereal.core.reservation.database.ReserveTermType?,
    val applyStartTime: java.time.LocalDateTime,
    val applyEndTime: java.time.LocalDateTime,
    val termStartTime: java.time.LocalDateTime,
    val termEndTime: java.time.LocalDateTime
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

internal data class ReserveTermAuditEvidence(
    val expected: ReserveTermDescriptor,
    val actualCandidates: List<ReserveTermCandidateEvidence>,
    val action: String = "preserved_fail_closed"
) {
    companion object {
        fun from(descriptor: ReserveTermDescriptor, candidates: Collection<ReserveTermEntity>) =
            ReserveTermAuditEvidence(descriptor, candidates.map(ReserveTermCandidateEvidence::from))
    }
}

@Service
class ReserveTermValidationService(
    private val reserveTermRepository: ReserveTermRepository,
    private val reserveTermPolicy: ReserveTermPolicy
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun findValidated(descriptor: ReserveTermDescriptor): ReserveTermEntity? {
        val audit = audit(descriptor)
        val validEntity = audit.validEntity?.takeIf {
            it.termYear == descriptor.termYear && it.termType == descriptor.termType
        }
        if (validEntity == null) {
            if (audit.validEntity != null) {
                logInvalid(audit.copy(validEntity = null, reason = "metadata_missing"))
            } else if (audit.reason != "missing") {
                logInvalid(audit)
            }
        }
        return validEntity
    }

    @Transactional(readOnly = true)
    fun findAllValidated(): List<ReserveTermEntity> {
        val rows = reserveTermRepository.findAllByOrderByApplyStartTimeAscTermStartTimeAscIdAsc()
        val descriptors = rows.mapNotNull { entity ->
            reserveTermPolicy.descriptorFor(entity).also { descriptor ->
                if (descriptor == null) {
                    val evidence = ReserveTermAuditEvidence.from(
                        reserveTermPolicy.descriptorFor(entity.termStartTime.toLocalDate()),
                        listOf(entity)
                    )
                    logger.error(
                        "event=reserve_term_invalid termYear={} termType={} reason=partial_metadata " +
                            "candidateIds={} expected={} actualCandidates={} action={}",
                        entity.termYear,
                        entity.termType,
                        listOf(entity.id),
                        evidence.expected,
                        evidence.actualCandidates,
                        evidence.action
                    )
                }
            }
        }.distinctBy { it.termYear to it.termType }

        val audits = descriptors.map { descriptor ->
            val keyedRows = rows.filter {
                it.termYear == descriptor.termYear && it.termType == descriptor.termType
            }
            val overlappingRows = rows.filter {
                it.termStartTime < descriptor.termEndTime && it.termEndTime > descriptor.termStartTime
            }
            reserveTermPolicy.audit(descriptor, keyedRows, overlappingRows)
        }
        audits.filter { it.validEntity == null && it.reason != "missing" }
            .forEach(::logInvalid)

        val validIds = audits.mapNotNull { audit ->
            audit.validEntity?.takeIf {
                it.termYear == audit.descriptor.termYear && it.termType == audit.descriptor.termType
            }?.id
        }.toSet()
        return rows.filter { it.id in validIds }
            .distinctBy { it.id }
            .sortedWith(compareBy<ReserveTermEntity> { it.applyStartTime }.thenBy { it.termStartTime }.thenBy { it.id })
    }

    fun audit(descriptor: ReserveTermDescriptor): ReserveTermAudit {
        val keyedRows = reserveTermRepository.findByTermYearAndTermType(descriptor.termYear, descriptor.termType)
        val overlappingRows = reserveTermRepository.findByTimeOverlap(descriptor.termStartTime, descriptor.termEndTime)
        return reserveTermPolicy.audit(descriptor, keyedRows, overlappingRows)
    }

    fun logInvalid(audit: ReserveTermAudit) {
        val evidence = ReserveTermAuditEvidence.from(audit.descriptor, audit.candidates)
        logger.error(
            "event=reserve_term_invalid termYear={} termType={} reason={} candidateIds={} " +
                "expected={} actualCandidates={} action={}",
            audit.descriptor.termYear,
            audit.descriptor.termType,
            audit.reason,
            audit.candidates.map { it.id },
            evidence.expected,
            evidence.actualCandidates,
            evidence.action
        )
    }
}
