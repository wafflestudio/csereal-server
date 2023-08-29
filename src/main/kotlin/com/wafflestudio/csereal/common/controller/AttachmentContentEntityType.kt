package com.wafflestudio.csereal.common.controller

import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity

interface AttachmentContentEntityType {
    fun bringAttachments(): List<AttachmentEntity>?
}