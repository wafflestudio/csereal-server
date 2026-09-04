package com.wafflestudio.csereal.core.about.dto

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.about.database.AboutEntity
import com.wafflestudio.csereal.core.about.database.AboutTranslationEntity

// 요청 본문과 같은 모양 — 사진은 동아리에 하나뿐이라 최상위, 이름·설명만 ko/en 안에.
data class GroupedClubDto(
    val id: Long,
    val imageURL: String?,
    val ko: ClubDto?,
    val en: ClubDto?
) {
    companion object {
        fun of(about: AboutEntity, imageURL: String?) = GroupedClubDto(
            id = about.id,
            imageURL = imageURL,
            ko = about.translationOf(LanguageType.KO)?.let { ClubDto.of(it) },
            en = about.translationOf(LanguageType.EN)?.let { ClubDto.of(it) }
        )
    }
}

data class ClubDto(
    val name: String,
    val description: String
) {
    companion object {
        fun of(translation: AboutTranslationEntity) = ClubDto(
            name = translation.name!!,
            description = translation.description
        )
    }
}
