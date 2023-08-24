package com.wafflestudio.csereal.core.reservation.dto

import com.wafflestudio.csereal.core.reservation.database.ReservationEntity
import java.time.LocalDateTime

data class ReservationDto(
    val id: Long,
    val title: String,
    val purpose: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val roomName: String?,
    val roomLocation: String,
    val userName: String,
    val contactEmail: String,
    val contactPhone: String
) {
    companion object {
        fun of(reservationEntity: ReservationEntity): ReservationDto {
            return ReservationDto(
                id = reservationEntity.id,
                title = reservationEntity.title,
                purpose = reservationEntity.purpose,
                startTime = reservationEntity.startTime,
                endTime = reservationEntity.endTime,
                roomName = reservationEntity.room.name,
                roomLocation = reservationEntity.room.location,
                userName = reservationEntity.user.username,
                contactEmail = reservationEntity.contactEmail,
                contactPhone = reservationEntity.contactPhone
            )
        }
    }
}
