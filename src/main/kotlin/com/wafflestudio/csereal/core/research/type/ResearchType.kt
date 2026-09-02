package com.wafflestudio.csereal.core.research.type

enum class ResearchRelatedType {
    RESEARCH_GROUP,
    RESEARCH_CENTER,
    LAB,
    CONFERENCE;

    fun ofResearchType() = when (this) {
        RESEARCH_GROUP -> ResearchType.GROUPS
        RESEARCH_CENTER -> ResearchType.CENTERS
        else -> throw IllegalArgumentException("ResearchRelatedType $this does not have corresponding ResearchType")
    }

    @com.fasterxml.jackson.annotation.JsonValue
    fun toValue() = name.lowercase().replace('_', '-')
}

enum class ResearchType(
    val krName: String
) {
    GROUPS("연구 그룹"),
    CENTERS("연구 센터");

    fun ofResearchRelatedType() = when (this) {
        GROUPS -> ResearchRelatedType.RESEARCH_GROUP
        CENTERS -> ResearchRelatedType.RESEARCH_CENTER
    }

    @com.fasterxml.jackson.annotation.JsonValue
    fun toValue() = name.lowercase().replace('_', '-')
}
