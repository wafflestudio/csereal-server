package com.wafflestudio.csereal.core.reservation.dto

import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import java.time.LocalDateTime

data class ReserveTermDto(
    val id: Long,
    val applyStartTime: LocalDateTime,
    val applyEndTime: LocalDateTime,
    val termStartTime: LocalDateTime,
    val termEndTime: LocalDateTime
) {
    companion object {
        fun of(entity: ReserveTermEntity): ReserveTermDto {
            return ReserveTermDto(
                id = entity.id,
                applyStartTime = entity.applyStartTime,
                applyEndTime = entity.applyEndTime,
                termStartTime = entity.termStartTime,
                termEndTime = entity.termEndTime
            )
        }
    }
}
