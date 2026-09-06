package com.wafflestudio.csereal.common.entity

import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity

interface AttachmentAttachable {
    var attachments: MutableList<AttachmentEntity>

    // 첨부 쪽 역방향 필드 이름이 엔티티마다 달라(attachment.news 등) 각자 구현한다.
    fun attach(attachment: AttachmentEntity)
}
