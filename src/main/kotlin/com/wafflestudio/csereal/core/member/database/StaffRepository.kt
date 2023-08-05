package com.wafflestudio.csereal.core.member.database

import org.springframework.data.jpa.repository.JpaRepository

interface StaffRepository : JpaRepository<StaffEntity, Long> {
}
