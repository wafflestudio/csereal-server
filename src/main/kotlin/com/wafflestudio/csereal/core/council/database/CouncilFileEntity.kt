package com.wafflestudio.csereal.core.council.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.domain.AttachmentAttachable
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
    override val attachments: MutableList<AttachmentEntity> = mutableListOf()
) : BaseTimeEntity(), AttachmentAttachable
