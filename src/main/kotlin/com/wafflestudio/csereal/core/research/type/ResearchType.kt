package com.wafflestudio.csereal.core.research.type

import com.wafflestudio.csereal.common.CserealException

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

    companion object {
        fun fromJsonValue(value: String) = try {
            ResearchType.valueOf(
                value.uppercase().replace('-', '_')
            )
        } catch (e: Exception) {
            throw CserealException.Csereal404("잘못된 Research Type이 주어졌습니다.")
        }
    }
}
