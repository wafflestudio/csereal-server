package com.wafflestudio.csereal.core.council.api.res

import com.wafflestudio.csereal.core.council.dto.CouncilFileRuleDto
import com.wafflestudio.csereal.core.council.dto.CouncilFileMeetingMinuteDto
import com.wafflestudio.csereal.core.council.type.CouncilFileRulesKey
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse

data class CouncilFileRuleResponse(
    val type: String,
    val attachments: List<AttachmentResponse>
) {
    init {
        require(
            type == CouncilFileRulesKey.CONSTITUTION.value() ||
                type == CouncilFileRulesKey.BYLAW.value()
        )
    }

    companion object {
        fun from(
            dto: CouncilFileRuleDto
        ): CouncilFileRuleResponse =
            CouncilFileRuleResponse(
                type = dto.key.value(),
                attachments = dto.attachments
            )
    }
}

data class CouncilFileRulesResponse(
    val constitution: CouncilFileRuleResponse?,
    val bylaw: CouncilFileRuleResponse?
) {
    init {
        require(
            constitution
                ?.let { it.type == CouncilFileRulesKey.CONSTITUTION.value() }
                ?: true
        )

        require(
            bylaw
                ?.let { it.type == CouncilFileRulesKey.BYLAW.value() }
                ?: true
        )
    }

    companion object {
        fun from(
            dtos: List<CouncilFileRuleDto>
        ): CouncilFileRulesResponse =
            dtos.groupBy { it.key }
                .let {
                    CouncilFileRulesResponse(
                        constitution = it[CouncilFileRulesKey.CONSTITUTION]
                            ?.firstOrNull()
                            ?.let { dto -> CouncilFileRuleResponse.from(dto) },
                        bylaw = it[CouncilFileRulesKey.BYLAW]
                            ?.firstOrNull()
                            ?.let { dto -> CouncilFileRuleResponse.from(dto) }
                    )
                }
    }
}

data class CouncilFileMeetingMinuteResponse(
    val year: Int,
    val index: Int,
    val attachments: List<AttachmentResponse>
) {
    companion object {
        fun from(
            dto: CouncilFileMeetingMinuteDto
        ): CouncilFileMeetingMinuteResponse =
            CouncilFileMeetingMinuteResponse(
                year = dto.year,
                index = dto.index,
                attachments = dto.attachments
            )
    }
}
