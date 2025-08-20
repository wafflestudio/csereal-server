package com.wafflestudio.csereal.core.resource.mainImage.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import jakarta.persistence.*

@Entity(name = "mainImage")
class MainImageEntity(
    var isDeleted: Boolean? = false,

    @Column(unique = true)
    val filename: String,

    val directory: String? = null,

    val imagesOrder: Int,
    val size: Long

) : BaseTimeEntity() {
    fun filePath(): String {
        return if (directory.isNullOrBlank()) return filename else "$directory/$filename"
    }
}
