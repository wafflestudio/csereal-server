package com.wafflestudio.csereal.core.about.dto

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.about.database.AboutEntity
import com.wafflestudio.csereal.core.about.database.AboutTranslationEntity

data class GroupedDirectionDto(
    val id: Long,
    val ko: DirDto?,
    val en: DirDto?
) {
    companion object {
        fun of(about: AboutEntity) = GroupedDirectionDto(
            id = about.id,
            ko = about.translationOf(LanguageType.KO)?.let { DirDto.of(it) },
            en = about.translationOf(LanguageType.EN)?.let { DirDto.of(it) }
        )
    }
}

data class DirDto(
    val name: String,
    val description: String
) {
    companion object {
        fun of(translation: AboutTranslationEntity) = DirDto(
            name = translation.name!!,
            description = translation.description
        )
    }
}
