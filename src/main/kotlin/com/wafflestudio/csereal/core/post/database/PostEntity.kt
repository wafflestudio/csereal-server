package com.wafflestudio.csereal.core.post.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity


@Entity(name = "post")
class PostEntity(
    @Column
    var title: String,

    @Column(columnDefinition = "text")
    var description: String
): BaseTimeEntity() {

}