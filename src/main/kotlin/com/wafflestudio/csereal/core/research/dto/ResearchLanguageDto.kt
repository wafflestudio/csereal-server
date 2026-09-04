package com.wafflestudio.csereal.core.research.dto

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.research.database.ResearchEntity
import com.wafflestudio.csereal.core.research.database.ResearchTranslationEntity
import com.wafflestudio.csereal.core.research.type.ResearchType

// 요청 본문과 같은 모양. 종류·웹사이트·대표이미지는 최상위, 이름·설명만 ko/en 안에.
data class ResearchLanguageDto(
    val id: Long,
    val type: ResearchType,
    val websiteURL: String?,
    val mainImageUrl: String?,
    val ko: ResearchTranslationDto?,
    val en: ResearchTranslationDto?
) {
    companion object {
        fun of(research: ResearchEntity, mainImageUrl: String?) = ResearchLanguageDto(
            id = research.id,
            type = research.postType,
            websiteURL = research.websiteURL,
            mainImageUrl = mainImageUrl,
            ko = research.translationOf(LanguageType.KO)?.let { ResearchTranslationDto.of(it) },
            en = research.translationOf(LanguageType.EN)?.let { ResearchTranslationDto.of(it) }
        )
    }
}

data class ResearchTranslationDto(
    val name: String,
    val description: String?,
    val labs: List<ResearchLabResponse>
) {
    companion object {
        fun of(translation: ResearchTranslationEntity) = ResearchTranslationDto(
            name = translation.name,
            description = translation.description,
            labs = translation.research.labs.mapNotNull { lab ->
                lab.translationOf(translation.language)?.let { ResearchLabResponse(lab.id, it.name) }
            }
        )
    }
}
