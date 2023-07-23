package com.wafflestudio.csereal.core.post.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity


@Entity(name = "post")
class PostEntity(
    @Column(nullable = true)
    var title: String? = null,

    @Column(columnDefinition = "text")
    var content: String
): BaseTimeEntity() {

}