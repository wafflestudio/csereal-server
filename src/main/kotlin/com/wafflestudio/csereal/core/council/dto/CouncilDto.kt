package com.wafflestudio.csereal.core.council.dto

import com.wafflestudio.csereal.core.council.type.CouncilType.CouncilDataType
import com.wafflestudio.csereal.core.council.type.CouncilType.CouncilSubType
import com.wafflestudio.csereal.core.council.type.CouncilType.CouncilType
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse
import java.time.LocalDateTime

sealed class CouncilDto(
    open val id: Long,
    open val type: CouncilType,
    open val createdAt: LocalDateTime?,
    open val modifiedAt: LocalDateTime?
) {
    data class CouncilAttachmentDto(
        override val id: Long,
        override val type: CouncilType,
        override val createdAt: LocalDateTime?,
        override val modifiedAt: LocalDateTime?,
        val attachments: List<AttachmentResponse>
    ) : CouncilDto(id, type, createdAt, modifiedAt) {
        init {
            require(type.dataType() == CouncilDataType.ATTACHMENT)
        }
    }

    // TODO: Create CouncilContentDto, CouncilContentAttachmentDto
}

sealed class CouncilCreateDto(
    open val type: CouncilType,
) {
    data class CouncilAttachmentCreateDto(
        override val type: CouncilType,
        val attachments: List<Long>
    ) : CouncilCreateDto(type) {
        init {
            require(type.dataType() == CouncilDataType.ATTACHMENT)
        }
    }

    // TODO: Create CouncilContentCreateDto, CouncilContentAttachmentCreateDto
}
