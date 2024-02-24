package com.wafflestudio.csereal.common.mockauth

import com.wafflestudio.csereal.core.user.database.UserEntity
import java.security.Principal

data class CustomPrincipal(val userEntity: UserEntity) : Principal {
    override fun getName(): String {
        return userEntity.username
    }
}
