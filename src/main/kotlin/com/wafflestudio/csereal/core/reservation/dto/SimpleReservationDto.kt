package com.wafflestudio.csereal.core.reservation.dto

import com.wafflestudio.csereal.core.reservation.database.ReservationEntity
import java.time.LocalDateTime

data class SimpleReservationDto(
    val id: Long,
    val title: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
) {
    companion object {
        fun of(reservationEntity: ReservationEntity): SimpleReservationDto {
            return SimpleReservationDto(
                id = reservationEntity.id,
                title = reservationEntity.title,
                startTime = reservationEntity.startTime,
                endTime = reservationEntity.endTime
            )
        }
    }
}
