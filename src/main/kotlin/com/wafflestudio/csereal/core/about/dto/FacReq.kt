package com.wafflestudio.csereal.core.about.dto

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.about.database.AboutEntity
import com.wafflestudio.csereal.core.about.database.AboutTranslationEntity

data class FacReq(
    val name: String,
    val description: String,
    val locations: MutableList<String>
)

data class FacDto(
    val name: String,
    val description: String,
    val locations: MutableList<String>
) {
    companion object {
        fun of(translation: AboutTranslationEntity) = FacDto(
            name = translation.name!!,
            description = translation.description,
            locations = translation.locations
        )
    }
}

data class GroupedFacDto(
    val id: Long,
    val imageURL: String?,
    val ko: FacDto?,
    val en: FacDto?
) {
    companion object {
        fun of(about: AboutEntity, imageURL: String?) = GroupedFacDto(
            id = about.id,
            imageURL = imageURL,
            ko = about.translationOf(LanguageType.KO)?.let { FacDto.of(it) },
            en = about.translationOf(LanguageType.EN)?.let { FacDto.of(it) }
        )
    }
}
