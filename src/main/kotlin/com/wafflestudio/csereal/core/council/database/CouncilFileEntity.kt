package com.wafflestudio.csereal.core.council.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.AttachmentContentEntityType
import com.wafflestudio.csereal.core.council.type.CouncilFileType
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import jakarta.persistence.*

@Entity(name = "council_file")
@Table(
    indexes = [
        Index(columnList = "type,key", unique = true)
    ]
)
class CouncilFileEntity(
    @Enumerated(EnumType.STRING)
    val type: CouncilFileType,

    @Column(name = "`key`")
    val key: String,

    @OneToMany(mappedBy = "councilFile", cascade = [CascadeType.ALL], orphanRemoval = true)
    val attachments: MutableList<AttachmentEntity> = mutableListOf()
) : BaseTimeEntity(), AttachmentContentEntityType {
    override fun bringAttachments(): List<AttachmentEntity> = attachments
}
