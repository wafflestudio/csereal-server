package com.wafflestudio.csereal.core.council.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.AttachmentContentEntityType
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity(name = "council")
@Table(
    indexes = [
        Index(columnList = "type, sub_type")
    ]
)
class CouncilEntity(
    // ...
) : BaseTimeEntity() {
    // ...
    val type: String,
    val subType: String? = null,

    @OneToMany(mappedBy = "council")
    val attachments: MutableSet<AttachmentEntity> = mutableSetOf(),
) : BaseTimeEntity(), AttachmentContentEntityType {
    override fun bringAttachments(): List<AttachmentEntity> {
        return attachments.toList()
    }
}
