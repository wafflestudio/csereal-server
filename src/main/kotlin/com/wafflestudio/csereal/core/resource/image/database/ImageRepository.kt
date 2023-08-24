package com.wafflestudio.csereal.core.resource.image.database

import com.wafflestudio.csereal.core.resource.image.database.ImageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ImageRepository : JpaRepository<ImageEntity, Long> {
    fun findByFilenameAndExtension(filename: String, extension: String): ImageEntity
}