package com.wafflestudio.csereal.core.reservation.dto

import java.time.LocalDateTime

data class ReserveRequest(
    val roomId: Long,
    val title: String,
    val contactEmail: String,
    val contactPhone: String,
    val purpose: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val recurringWeeks: Int? = null
)
