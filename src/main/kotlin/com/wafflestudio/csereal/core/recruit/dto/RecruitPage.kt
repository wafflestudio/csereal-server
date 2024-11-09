package com.wafflestudio.csereal.core.recruit.dto

import com.wafflestudio.csereal.core.recruit.database.RecruitEntity

data class RecruitPage(
    val title: String,
    val description: String,
    val mainImageUrl: String?
) {
    companion object {
        private val emptyPage = RecruitPage("", "", null)

        fun empty() = emptyPage

        fun of(
            recruitEntity: RecruitEntity,
            mainImageUrl: String?
        ): RecruitPage =
            RecruitPage(
                title = recruitEntity.title,
                description = recruitEntity.description,
                mainImageUrl = mainImageUrl
            )
    }
}
