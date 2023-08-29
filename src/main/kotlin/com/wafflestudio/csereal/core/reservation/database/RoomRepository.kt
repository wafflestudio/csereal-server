package com.wafflestudio.csereal.core.reservation.database

import org.springframework.data.jpa.repository.JpaRepository

interface RoomRepository : JpaRepository<RoomEntity, Long> {
}
