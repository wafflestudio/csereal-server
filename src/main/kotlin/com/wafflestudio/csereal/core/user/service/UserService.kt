package com.wafflestudio.csereal.core.user.service

import com.wafflestudio.csereal.core.user.database.UserRepository
import com.wafflestudio.csereal.core.user.dto.LoginRequest
import org.springframework.stereotype.Service

interface UserService {
    fun login(loginRequest: LoginRequest)
}

@Service
class UserServiceImpl(private val userRepository: UserRepository) : UserService {

    override fun login(loginRequest: LoginRequest) {

    }

}
