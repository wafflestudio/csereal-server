package com.wafflestudio.csereal.common.entity

import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity

interface AttachmentAttachable {
    val attachments: List<AttachmentEntity>

    fun getAttachmentFolder(): String
}
