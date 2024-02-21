package com.wafflestudio.csereal.common.mockauth

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.user.database.UserRepository
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

@Component
class DevAuthenticationProvider(private val userRepository: UserRepository) : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication? {
        val username = authentication.name
        val userEntity =
            userRepository.findByUsername(username) ?: throw CserealException.Csereal404("Mock User not found")

        val customPrincipal = CustomPrincipal(userEntity)
        return UsernamePasswordAuthenticationToken(customPrincipal, null, listOf(SimpleGrantedAuthority("ROLE_STAFF")))
    }

    override fun supports(authentication: Class<*>): Boolean {
        return UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}

