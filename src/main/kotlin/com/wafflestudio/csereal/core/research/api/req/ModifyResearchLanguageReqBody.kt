package com.wafflestudio.csereal.core.research.api.req

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.wafflestudio.csereal.core.research.type.ResearchType

data class ModifyResearchLanguageReqBody(
    val ko: ModifyResearchSealedReqBody,
    val en: ModifyResearchSealedReqBody,
) {
    fun valid() = ko.type == en.type
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ModifyResearchGroupReqBody::class, names = ["GROUPS", "groups"]),
    JsonSubTypes.Type(value = ModifyResearchCenterReqBody::class, names = ["CENTERS", "centers"]),
)
sealed class ModifyResearchSealedReqBody(
    val type: ResearchType,
    open val name: String,
    open val description: String,
    open val removeImage: Boolean,
)

data class ModifyResearchGroupReqBody(
    override val name: String,
    override val description: String,
    override val removeImage: Boolean,
) : ModifyResearchSealedReqBody(ResearchType.GROUPS, name, description, removeImage) {
}

data class ModifyResearchCenterReqBody(
    override val name: String,
    override val description: String,
    override val removeImage: Boolean,
    val websiteURL: String?,
) : ModifyResearchSealedReqBody(ResearchType.CENTERS, name, description, removeImage) {
}
