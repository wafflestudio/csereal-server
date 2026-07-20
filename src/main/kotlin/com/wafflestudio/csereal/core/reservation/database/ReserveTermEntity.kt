package com.wafflestudio.csereal.core.reservation.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.time.LocalDateTime

@Entity(name = "reserve_term")
class ReserveTermEntity(
    val applyStartTime: LocalDateTime,
    val applyEndTime: LocalDateTime,

    val termStartTime: LocalDateTime,
    val termEndTime: LocalDateTime,

    @Column(updatable = false)
    val termYear: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 32, updatable = false)
    val termType: ReserveTermType? = null
) : BaseTimeEntity()
