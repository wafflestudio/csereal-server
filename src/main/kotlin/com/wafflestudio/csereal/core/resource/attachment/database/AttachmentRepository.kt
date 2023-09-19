package com.wafflestudio.csereal.core.resource.attachment.database

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AttachmentRepository : JpaRepository<AttachmentEntity, Long> {
    fun findByFilename(filename: String): AttachmentEntity
}
