package com.wafflestudio.csereal.core.scholarship.database

import org.springframework.data.jpa.repository.JpaRepository

interface ScholarshipRepository : JpaRepository<ScholarshipEntity, Long> {
}
