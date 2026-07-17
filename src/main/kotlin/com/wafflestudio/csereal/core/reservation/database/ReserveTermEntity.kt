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

    var termYear: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    var termType: ReserveTermType? = null
) : BaseTimeEntity() {
    fun assignCanonicalMetadata(termYear: Int, termType: ReserveTermType) {
        require(this.termYear == null && this.termType == null) { "Reserve term metadata is already assigned" }
        this.termYear = termYear
        this.termType = termType
    }
}
