package com.wafflestudio.csereal.core.user.service

import com.wafflestudio.csereal.core.user.database.Role
import com.wafflestudio.csereal.core.user.database.UserEntity
import com.wafflestudio.csereal.core.user.database.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomOidcUserService(
    private val userRepository: UserRepository
) : OAuth2UserService<OidcUserRequest, OidcUser> {

    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
        val oidcUser = DefaultOidcUser(
            userRequest.clientRegistration.scopes.map { SimpleGrantedAuthority("SCOPE_$it") },
            userRequest.idToken
        )

        createUserIfNotExists(oidcUser.idToken)

        return oidcUser
    }

    @Transactional
    fun createUserIfNotExists(idToken: OidcIdToken) {
        val userEmail = idToken.getClaim<String>("email")
        val user = userRepository.findByEmail(userEmail)

        if (user == null) {

            // TODO: 권한 추가
            // val role = idToken.getClaim<List<String>>("groups")

            val newUser = UserEntity(
                name = idToken.getClaim("name"),
                email = userEmail,
                role = Role.ROLE_ADMIN
            )
            userRepository.save(newUser)
        }
    }
}
