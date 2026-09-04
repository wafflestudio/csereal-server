package com.wafflestudio.csereal.core.research.dto

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.research.database.ResearchTranslationEntity
import com.wafflestudio.csereal.core.research.type.ResearchType

// 목록용. 한 언어판을 평평하게 편 모양이다(편집용 ResearchLanguageDto 와 용도가 다르다).
sealed class ResearchSealedDto(
    val type: ResearchType,
    open val id: Long,
    open val language: LanguageType,
    open val name: String,
    open val description: String,
    open val mainImageUrl: String?
) {
    fun valid(researchType: ResearchType) = this.type == researchType

    companion object {
        fun of(translation: ResearchTranslationEntity, imageUrl: String?) =
            when (translation.research.postType) {
                ResearchType.GROUPS -> ResearchGroupDto.of(translation, imageUrl)
                ResearchType.CENTERS -> ResearchCenterDto.of(translation, imageUrl)
            }
    }
}

data class ResearchGroupDto(
    override val id: Long,
    override val language: LanguageType,
    override val name: String,
    override val description: String,
    override val mainImageUrl: String?,
    val labs: List<ResearchLabResponse>
) : ResearchSealedDto(ResearchType.GROUPS, id, language, name, description, mainImageUrl) {
    companion object {
        fun of(translation: ResearchTranslationEntity, imageUrl: String?) = ResearchGroupDto(
            id = translation.research.id,
            language = translation.language,
            name = translation.name,
            description = translation.description!!,
            mainImageUrl = imageUrl,
            labs = translation.research.labs.mapNotNull { lab ->
                lab.translationOf(translation.language)?.let { ResearchLabResponse(lab.id, it.name) }
            }
        )
    }
}

data class ResearchCenterDto(
    override val id: Long,
    override val language: LanguageType,
    override val name: String,
    override val description: String,
    override val mainImageUrl: String?,
    val websiteURL: String?
) : ResearchSealedDto(ResearchType.CENTERS, id, language, name, description, mainImageUrl) {
    companion object {
        fun of(translation: ResearchTranslationEntity, imageUrl: String?) = ResearchCenterDto(
            id = translation.research.id,
            language = translation.language,
            name = translation.name,
            description = translation.description!!,
            mainImageUrl = imageUrl,
            websiteURL = translation.research.websiteURL
        )
    }
}
