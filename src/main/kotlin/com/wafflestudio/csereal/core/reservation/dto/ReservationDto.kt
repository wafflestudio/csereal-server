package com.wafflestudio.csereal.core.reservation.dto

import com.wafflestudio.csereal.core.reservation.database.ReservationEntity
import com.wafflestudio.csereal.core.reservation.database.ReservationType
import java.time.LocalDateTime
import java.util.UUID

data class ReservationDto(
    val id: Long,
    val recurrenceId: UUID,
    val title: String,
    val purpose: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val recurringWeeks: Int = 1,
    val reservationType: ReservationType?,
    val roomName: String?,
    val roomLocation: String,
    val userName: String? = null,
    val contactEmail: String? = null,
    val contactPhone: String? = null,
    val professor: String
) {
    companion object {
        fun of(entity: ReservationEntity): ReservationDto = from(entity, includeContact = true)

        fun forNormalUser(entity: ReservationEntity): ReservationDto = from(entity, includeContact = false)

        private fun from(entity: ReservationEntity, includeContact: Boolean): ReservationDto {
            return ReservationDto(
                id = entity.id,
                recurrenceId = entity.recurrenceId,
                title = entity.title,
                purpose = entity.purpose,
                startTime = entity.startTime,
                endTime = entity.endTime,
                recurringWeeks = entity.recurringWeeks,
                reservationType = entity.reservationType,
                roomName = entity.room.name,
                roomLocation = entity.room.location,
                userName = entity.user.username.takeIf { includeContact },
                contactEmail = entity.contactEmail.takeIf { includeContact },
                contactPhone = entity.contactPhone.takeIf { includeContact },
                professor = entity.professor
            )
        }
    }
}
