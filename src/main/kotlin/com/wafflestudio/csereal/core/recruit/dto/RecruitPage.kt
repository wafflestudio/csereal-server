package com.wafflestudio.csereal.core.recruit.dto

import com.wafflestudio.csereal.core.recruit.database.RecruitEntity

data class RecruitPage(
    val latestRecruitTitle: String,
    val latestRecruitUrl: String,
    val description: String,
    val mainImageUrl: String?
) {
    companion object {
        private val emptyPage = RecruitPage("", "", "", null)
        fun empty() = emptyPage

        fun of(recruitEntity: RecruitEntity, mainImageUrl: String?): RecruitPage {
            return RecruitPage(
                latestRecruitTitle = recruitEntity.latestRecruitTitle,
                latestRecruitUrl = recruitEntity.latestRecruitUrl,
                description = recruitEntity.description,
                mainImageUrl = mainImageUrl
            )
        }
    }
}
