package com.wafflestudio.csereal.core.user.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Entity(name = "users")
class UserEntity(

    val name: String,
    val email: String,

    @Enumerated(EnumType.STRING)
    val role: Role,

    ) : BaseTimeEntity()

enum class Role {
    ROLE_ADMIN, ROLE_GRADUATE, ROLE_LAB_ASSISTANT
}
