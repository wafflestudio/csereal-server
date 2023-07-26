package com.wafflestudio.csereal.core.notice.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Entity

@Entity(name = "tag")
class TagEntity(
    var name: String
) : BaseTimeEntity() {
}