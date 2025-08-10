package com.wafflestudio.csereal.common.domain

import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity

interface AttachmentAttachable {
    val attachments: List<AttachmentEntity>
}
