package com.wafflestudio.csereal.core.council.dto

import com.wafflestudio.csereal.core.council.database.CouncilFileEntity
import com.wafflestudio.csereal.core.council.type.CouncilFileMeetingMinuteKey
import com.wafflestudio.csereal.core.council.type.CouncilFileRulesKey
import com.wafflestudio.csereal.core.council.type.CouncilFileType
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse

data class CouncilFileDto(
    val id: Long,
    val type: CouncilFileType,
    val key: String,
    val attachments: List<AttachmentResponse>
) {
    companion object {
        fun from(
            entity: CouncilFileEntity,
            attachmentResponseProvider: (List<AttachmentEntity>) -> List<AttachmentResponse>
        ): CouncilFileDto {
            return CouncilFileDto(
                id = entity.id,
                type = entity.type,
                key = entity.key,
                attachments = attachmentResponseProvider(entity.attachments)
            )
        }
    }
}

data class CouncilFileRuleDto(
    val id: Long,
    val key: CouncilFileRulesKey,
    val attachments: List<AttachmentResponse>
) {
    val type: CouncilFileType = CouncilFileType.RULE

    fun to(): CouncilFileDto {
        return CouncilFileDto(
            id = id,
            type = type,
            key = key.value(),
            attachments = attachments
        )
    }

    companion object {
        fun from(
            dto: CouncilFileDto
        ): CouncilFileRuleDto {
            return CouncilFileRuleDto(
                id = dto.id,
                key = CouncilFileRulesKey.from(dto.key),
                attachments = dto.attachments
            )
        }
    }
}

data class CouncilFileMeetingMinuteDto(
    val id: Long,
    val year: Int,
    val index: Int,
    val attachments: List<AttachmentResponse>
) {
    val type: CouncilFileType = CouncilFileType.MEETING_MINUTE

    fun to(): CouncilFileDto {
        return CouncilFileDto(
            id = id,
            type = type,
            key = CouncilFileMeetingMinuteKey(year, index).value(),
            attachments = attachments
        )
    }

    companion object {
        fun from(
            dto: CouncilFileDto
        ): CouncilFileMeetingMinuteDto {
            val key = CouncilFileMeetingMinuteKey.from(dto.key)

            return CouncilFileMeetingMinuteDto(
                id = dto.id,
                year = key.year,
                index = key.index,
                attachments = dto.attachments
            )
        }
    }
}
