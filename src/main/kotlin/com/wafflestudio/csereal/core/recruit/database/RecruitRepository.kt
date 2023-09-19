package com.wafflestudio.csereal.core.recruit.database

import org.springframework.data.jpa.repository.JpaRepository

interface RecruitRepository : JpaRepository<RecruitEntity, Long>
