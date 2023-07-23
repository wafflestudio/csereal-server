package com.wafflestudio.csereal.core.user.database

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class UserEntity(
    @Id
    val id: Long,
    val username: String,
    val email: String,
    val role: String
)
