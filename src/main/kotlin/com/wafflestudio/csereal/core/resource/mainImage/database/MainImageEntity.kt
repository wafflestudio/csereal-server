package com.wafflestudio.csereal.core.resource.mainImage.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.*


@Entity(name = "mainImage")
class MainImageEntity(
    val isDeleted : Boolean? = false,

    @Column(unique = true)
    val filename: String,

    val imagesOrder: Int,
    val size: Long,

    ) : BaseTimeEntity() {

}