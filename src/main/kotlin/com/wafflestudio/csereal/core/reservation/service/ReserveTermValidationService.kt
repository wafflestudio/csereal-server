package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReserveTermValidationService(
    private val reserveTermRepository: ReserveTermRepository,
    private val reserveTermPolicy: ReserveTermPolicy
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun findValidated(descriptor: ReserveTermDescriptor): ReserveTermEntity? {
        val audit = audit(descriptor)
        if (audit.validEntity == null && audit.reason != "missing") {
            logInvalid(audit)
        }
        return audit.validEntity
    }

    @Transactional(readOnly = true)
    fun findAllValidated(): List<ReserveTermEntity> {
        val rows = reserveTermRepository.findAllByOrderByApplyStartTimeAscTermStartTimeAscIdAsc()
        val descriptors = rows.mapNotNull { entity ->
            reserveTermPolicy.descriptorFor(entity).also { descriptor ->
                if (descriptor == null) {
                    logger.error(
                        "event=reserve_term_invalid reason=partial_metadata termId={} " +
                            "termYear={} termType={} candidateIds={}",
                        entity.id,
                        entity.termYear,
                        entity.termType,
                        listOf(entity.id)
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

        val validIds = audits.mapNotNull { it.validEntity?.id }.toSet()
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
        logger.error(
            "event=reserve_term_invalid termYear={} termType={} reason={} candidateIds={}",
            audit.descriptor.termYear,
            audit.descriptor.termType,
            audit.reason,
            audit.candidates.map { it.id }
        )
    }
}
