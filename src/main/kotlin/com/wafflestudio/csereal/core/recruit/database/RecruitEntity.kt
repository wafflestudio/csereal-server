package com.wafflestudio.csereal.core.recruit.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity(name = "recruit")
class RecruitEntity(
    val latestRecruitTitle: String,
    val latestRecruitUrl: String,

    @Column(columnDefinition = "text")
    val description: String
) : BaseTimeEntity()
