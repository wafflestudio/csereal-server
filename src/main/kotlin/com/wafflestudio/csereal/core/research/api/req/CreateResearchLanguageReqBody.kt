package com.wafflestudio.csereal.core.research.api.req

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.wafflestudio.csereal.core.research.type.ResearchType

data class CreateResearchLanguageReqBody(
    val ko: CreateResearchSealedReqBody,
    val en: CreateResearchSealedReqBody
) {
    fun valid() = ko.type == en.type
    fun valid(type: ResearchType) = ko.valid(type) && en.valid(type)
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = CreateResearchGroupReqBody::class, names = ["GROUPS", "groups"]),
    JsonSubTypes.Type(value = CreateResearchCenterReqBody::class, names = ["CENTERS", "centers"])
)
sealed class CreateResearchSealedReqBody(
    val type: ResearchType,
    open val name: String,
    open val description: String,
    open val mainImageUrl: String?
) {
    fun valid(type: ResearchType) = this.type == type
}

data class CreateResearchGroupReqBody(
    override val name: String,
    override val description: String,
    override val mainImageUrl: String?
) : CreateResearchSealedReqBody(ResearchType.GROUPS, name, description, mainImageUrl)

data class CreateResearchCenterReqBody(
    override val name: String,
    override val description: String,
    override val mainImageUrl: String?,
    val websiteURL: String?
) : CreateResearchSealedReqBody(ResearchType.CENTERS, name, description, mainImageUrl)
