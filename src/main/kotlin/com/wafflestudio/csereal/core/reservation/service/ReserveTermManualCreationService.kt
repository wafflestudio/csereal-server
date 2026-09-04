package com.wafflestudio.csereal.core.reservation.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermRepository
import com.wafflestudio.csereal.core.reservation.dto.CreateCustomReserveTermRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReserveTermManualCreationService(
    private val reserveTermRepository: ReserveTermRepository,
    private val reserveTermPolicy: ReserveTermPolicy
) {
    @Transactional
    fun createCustom(request: CreateCustomReserveTermRequest): ReserveTermEntity {
        val candidate = ReserveTermEntity(
            applyStartTime = request.applyStartTime,
            applyEndTime = request.applyEndTime,
            termStartTime = request.termStartTime,
            termEndTime = request.termEndTime,
            termYear = null,
            termType = null
        )
        val invalidReasons = reserveTermPolicy.invalidReasons(candidate)
        if (invalidReasons.isNotEmpty()) {
            throw CserealException(ErrorCode.VALIDATION_FAILED, mapOf("reasons" to invalidReasons.joinToString(",")))
        }

        if (reserveTermRepository.findByTimeOverlap(request.termStartTime, request.termEndTime).isNotEmpty()) {
            throw CserealException(ErrorCode.TERM_OVERLAP)
        }

        return reserveTermRepository.saveAndFlush(candidate)
    }
}
