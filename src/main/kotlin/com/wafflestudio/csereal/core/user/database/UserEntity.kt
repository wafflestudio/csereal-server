package com.wafflestudio.csereal.core.user.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Entity

@Entity(name = "users")
class UserEntity(

    val username: String,
    val name: String,
    val email: String,
    val studentId: String

) : BaseTimeEntity()
