package com.wafflestudio.csereal.core.resource.attachment.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.*

@Entity(name = "attachment")
class AttachmentEntity(
    val isDeleted : Boolean? = true,

    @Column(unique = true)
    val filename: String,

    val attachmentsOrder: Int,
    val size: Long,

    ) : BaseTimeEntity() {

}