package com.wafflestudio.csereal.core.user.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.user.database.Role
import com.wafflestudio.csereal.core.user.database.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface UserService {
    fun checkStaffAuth(username: String): Boolean
    fun checkReservationAuth(username: String): Boolean
}

@Service
@Transactional
class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {

    @Transactional(readOnly = true)
    override fun checkStaffAuth(username: String): Boolean {
        val user = userRepository.findByUsername(username) ?: throw CserealException.Csereal404("재로그인이 필요합니다.")
        return user.role == Role.ROLE_STAFF
    }

    @Transactional(readOnly = true)
    override fun checkReservationAuth(username: String): Boolean {
        val user = userRepository.findByUsername(username) ?: throw CserealException.Csereal404("재로그인이 필요합니다.")
        return user.role != null
    }
}
