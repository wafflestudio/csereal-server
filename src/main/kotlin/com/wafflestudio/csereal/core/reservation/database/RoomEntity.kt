package com.wafflestudio.csereal.core.reservation.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Entity(name = "room")
class RoomEntity(
    val name: String?,
    val location: String,

    val capacity: Int,

    @Enumerated(EnumType.STRING)
    val type: RoomType
) : BaseTimeEntity()

enum class RoomType {
    SEMINAR, LAB, LECTURE
}
