package com.wafflestudio.csereal.core.reservation.database

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.reservation.dto.ReserveRequest
import com.wafflestudio.csereal.core.user.database.UserEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.time.LocalDateTime
import java.util.*

@Entity(name = "reservation")
class ReservationEntity(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    val room: RoomEntity,

    val title: String,
    val contactEmail: String,
    val contactPhone: String,
    val purpose: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val professor: String,
    val recurringWeeks: Int = 1,

    val recurrenceId: UUID? = null

) : BaseTimeEntity() {

    @PrePersist
    @PreUpdate
    fun validateDates() {
        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            throw CserealException.Csereal400("종료 시각은 시작 시각 이후여야 합니다.")
        }
    }

    companion object {
        fun create(
            user: UserEntity,
            room: RoomEntity,
            reserveRequest: ReserveRequest,
            start: LocalDateTime,
            end: LocalDateTime,
            recurrenceId: UUID
        ): ReservationEntity {
            return ReservationEntity(
                user = user,
                room = room,
                title = reserveRequest.title,
                contactEmail = reserveRequest.contactEmail,
                contactPhone = reserveRequest.contactPhone,
                purpose = reserveRequest.purpose,
                startTime = start,
                endTime = end,
                professor = reserveRequest.professor,
                recurringWeeks = reserveRequest.recurringWeeks,
                recurrenceId = recurrenceId
            )
        }
    }

}
