package com.wafflestudio.csereal.core.reservation.dto

import com.wafflestudio.csereal.core.reservation.database.ReservationEntity
import java.time.LocalDateTime
import java.util.UUID

data class ReservationDto(
    val id: Long,
    val recurrenceId: UUID? = null,
    val title: String,
    val purpose: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val recurringWeeks: Int = 1,
    val roomName: String?,
    val roomLocation: String,
    val userName: String? = null,
    val contactEmail: String? = null,
    val contactPhone: String? = null,
    val professor: String
) {
    companion object {
        fun of(reservationEntity: ReservationEntity): ReservationDto {
            return ReservationDto(
                id = reservationEntity.id,
                recurrenceId = reservationEntity.recurrenceId,
                title = reservationEntity.title,
                purpose = reservationEntity.purpose,
                startTime = reservationEntity.startTime,
                endTime = reservationEntity.endTime,
                recurringWeeks = reservationEntity.recurringWeeks,
                roomName = reservationEntity.room.name,
                roomLocation = reservationEntity.room.location,
                userName = reservationEntity.user.username,
                contactEmail = reservationEntity.contactEmail,
                contactPhone = reservationEntity.contactPhone,
                professor = reservationEntity.professor
            )
        }

        fun forNormalUser(reservationEntity: ReservationEntity): ReservationDto {
            return ReservationDto(
                id = reservationEntity.id,
                title = reservationEntity.title,
                purpose = reservationEntity.purpose,
                startTime = reservationEntity.startTime,
                endTime = reservationEntity.endTime,
                recurringWeeks = reservationEntity.recurringWeeks,
                roomName = reservationEntity.room.name,
                roomLocation = reservationEntity.room.location,
                professor = reservationEntity.professor
            )
        }

    }
}
