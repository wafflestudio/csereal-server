package com.wafflestudio.csereal.core.user.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Entity(name = "users")
class UserEntity(

    val username: String,
    val name: String,
    val email: String,
    val studentId: String,

    @Enumerated(EnumType.STRING)
    var role: Role?

) : BaseTimeEntity()

enum class Role {
    ROLE_STAFF, ROLE_GRADUATE, ROLE_PROFESSOR
}
