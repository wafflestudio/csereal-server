package com.wafflestudio.csereal.core.council.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.AttachmentContentEntityType
import com.wafflestudio.csereal.core.council.dto.CouncilDto
import com.wafflestudio.csereal.core.council.type.CouncilType.CouncilDataType
import com.wafflestudio.csereal.core.council.type.CouncilType.CouncilType
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.resource.attachment.dto.AttachmentResponse
import jakarta.persistence.*

@Entity(name = "council")
@Table(
    indexes = [
        Index(columnList = "type, sub_type")
    ]
)
class CouncilEntity(
    val type: String,
    val subType: String? = null,

    @Column(columnDefinition = "VARCHAR(255)")
    @Enumerated(EnumType.STRING)
    val dataType: CouncilDataType,

    @OneToMany(mappedBy = "council")
    val attachments: MutableSet<AttachmentEntity> = mutableSetOf(),
) : BaseTimeEntity(), AttachmentContentEntityType {
    override fun bringAttachments(): List<AttachmentEntity> {
        return attachments.toList()
    }

    // TODO: Check type's data type when create

    fun toDto(attachmentResponseMapper: (List<AttachmentEntity>) -> List<AttachmentResponse>): CouncilDto {
        val type = CouncilType.fromString(this.type, this.subType)

        return when (type.dataType()) {
            CouncilDataType.ATTACHMENT -> CouncilDto.CouncilAttachmentDto(
                id = id,
                type = type,
                createdAt = createdAt,
                modifiedAt = modifiedAt,
                attachments = attachmentResponseMapper(this.attachments.toList())
            )

            else -> TODO("IMPLEMENT")
        }
    }
}
