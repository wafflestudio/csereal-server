package com.wafflestudio.csereal.core.user.service

import com.wafflestudio.csereal.common.mockauth.CustomOidcUser
import com.wafflestudio.csereal.core.user.database.UserEntity
import com.wafflestudio.csereal.core.user.database.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository
) {
    @Transactional
    fun getLoginUser(): UserEntity {
        val auth = SecurityContextHolder.getContext().authentication
            ?: return userRepository.findByUsername("test")!! // Please create test user before test when needed
        return when (val principal = auth.principal) {
            is CustomOidcUser -> principal.userEntity
            else -> throw IllegalStateException("Unexpected principal type: ${principal::class.java}")
        }
    }
}
