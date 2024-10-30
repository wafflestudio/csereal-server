package com.wafflestudio.csereal.core.reservation.database

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface RoomRepository : JpaRepository<RoomEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findRoomById(id: Long): RoomEntity?
}
