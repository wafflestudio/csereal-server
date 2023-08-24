package com.wafflestudio.csereal.core.resource.image.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.notice.database.NoticeEntity
import jakarta.persistence.*


@Entity(name = "image")
class ImageEntity(
    val isDeleted : Boolean? = true,

    @Column(unique = true)
    val filename: String,

    val extension: String,
    val imagesOrder: Int,
    val size: Long,

    ) : BaseTimeEntity() {

}