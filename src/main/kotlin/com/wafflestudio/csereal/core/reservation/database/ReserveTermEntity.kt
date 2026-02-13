package com.wafflestudio.csereal.core.reservation.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import jakarta.persistence.Entity
import java.time.LocalDateTime

@Entity(name = "reserve_term")
class ReserveTermEntity(
    val applyStartTime: LocalDateTime,
    val applyEndTime: LocalDateTime,

    val termStartTime: LocalDateTime,
    val termEndTime: LocalDateTime
) : BaseTimeEntity()
