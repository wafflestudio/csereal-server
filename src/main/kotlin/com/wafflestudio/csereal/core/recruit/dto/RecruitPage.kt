package com.wafflestudio.csereal.core.recruit.dto

import com.wafflestudio.csereal.core.recruit.database.RecruitEntity


data class RecruitPage(
    val latestRecruitTitle: String,
    val latestRecruitUrl: String,
    val description: String
) {
    companion object {
        fun of(recruitEntity: RecruitEntity): RecruitPage {
            return RecruitPage(
                latestRecruitTitle = recruitEntity.latestRecruitTitle,
                latestRecruitUrl = recruitEntity.latestRecruitUrl,
                description = recruitEntity.description
            )
        }
    }
}
