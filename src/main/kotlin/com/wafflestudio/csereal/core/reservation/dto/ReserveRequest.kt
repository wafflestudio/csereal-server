package com.wafflestudio.csereal.core.reservation.dto

import com.wafflestudio.csereal.core.reservation.database.ReservationType
import java.time.LocalDateTime

data class ReserveRequest(
    val roomId: Long,
    val title: String,
    val contactEmail: String,
    val contactPhone: String,
    val professor: String,
    val purpose: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val agreed: Boolean,
    val recurringWeeks: Int = 1,
    val reservationType: ReservationType? = null
)
