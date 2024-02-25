package com.wafflestudio.csereal.core.research.dto

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.research.database.ResearchSearchEntity
import com.wafflestudio.csereal.core.research.database.ResearchSearchType

data class ResearchSearchResponseElement(
    val id: Long,
    val name: String,
    val researchType: ResearchSearchType
) {
    companion object {
        fun of(
            researchSearchEntity: ResearchSearchEntity
        ): ResearchSearchResponseElement =
            when {
                researchSearchEntity.research != null &&
                    researchSearchEntity.lab == null &&
                    researchSearchEntity.conferenceElement == null
                -> researchSearchEntity.research!!.let {
                    ResearchSearchResponseElement(
                        id = it.id,
                        name = it.name,
                        researchType = ResearchSearchType.RESEARCH
                    )
                }

                researchSearchEntity.lab != null &&
                    researchSearchEntity.research == null &&
                    researchSearchEntity.conferenceElement == null
                -> researchSearchEntity.lab!!.let {
                    ResearchSearchResponseElement(
                        id = it.id,
                        name = it.name,
                        researchType = ResearchSearchType.LAB
                    )
                }

                researchSearchEntity.conferenceElement != null &&
                    researchSearchEntity.research == null &&
                    researchSearchEntity.lab == null
                -> researchSearchEntity.conferenceElement!!.let {
                    ResearchSearchResponseElement(
                        id = it.id,
                        name = it.name,
                        researchType = ResearchSearchType.CONFERENCE
                    )
                }

                else -> throw CserealException.Csereal401(
                    "ResearchSearchEntity의 연결이 올바르지 않습니다."
                )
            }
    }
}
