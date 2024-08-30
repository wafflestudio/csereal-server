package com.wafflestudio.csereal.core.research.dto

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.research.database.ResearchEntity
import com.wafflestudio.csereal.core.research.type.ResearchType

data class ResearchLanguageDto(
    val ko: ResearchSealedDto,
    val en: ResearchSealedDto,
) {
    fun valid() = ko.type == en.type
    fun valid(researchType: ResearchType) = ko.valid(researchType) && en.valid(researchType)
}

sealed class ResearchSealedDto(
    val type: ResearchType,
    open val id: Long,
    open val language: LanguageType,
    open val name: String,
    open val description: String,
    open val mainImageUrl: String?,
) {
    fun valid(researchType: ResearchType) = this.type == researchType

    companion object {
        fun of(entity: ResearchEntity, imageUrl: String?) = when (entity.postType) {
            ResearchType.GROUPS -> ResearchGroupDto.of(entity, imageUrl)
            ResearchType.CENTERS -> ResearchCenterDto.of(entity, imageUrl)
        }
    }
}

data class ResearchGroupDto(
    override val id: Long,
    override val language: LanguageType,
    override val name: String,
    override val description: String,
    override val mainImageUrl: String?,
    val labs: List<ResearchLabResponse>,
) : ResearchSealedDto(ResearchType.GROUPS, id, language, name, description, mainImageUrl) {
    companion object {
        fun of(entity: ResearchEntity, imageUrl: String?) = ResearchGroupDto(
            id = entity.id,
            language = entity.language,
            name = entity.name,
            description = entity.description!!,
            mainImageUrl = imageUrl,
            labs = entity.labs.map { ResearchLabResponse(it.id, it.name) }
        )
    }
}

data class ResearchCenterDto(
    override val id: Long,
    override val language: LanguageType,
    override val name: String,
    override val description: String,
    override val mainImageUrl: String?,
    val websiteURL: String?,
) : ResearchSealedDto(ResearchType.CENTERS, id, language, name, description, mainImageUrl) {
    companion object {
        fun of(entity: ResearchEntity, imageUrl: String?) = ResearchCenterDto(
            id = entity.id,
            language = entity.language,
            name = entity.name,
            description = entity.description!!,
            mainImageUrl = imageUrl,
            websiteURL = entity.websiteURL,
        )
    }
}
