package com.wafflestudio.csereal.core.user.service

import com.wafflestudio.csereal.core.user.database.Role
import com.wafflestudio.csereal.core.user.database.UserEntity
import com.wafflestudio.csereal.core.user.database.UserRepository
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange

@Service
class CustomOidcUserService(
    private val userRepository: UserRepository,
    private val restTemplate: RestTemplate
) : OAuth2UserService<OidcUserRequest, OidcUser> {

    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
        val oidcUser = DefaultOidcUser(
            userRequest.clientRegistration.scopes.map { SimpleGrantedAuthority("SCOPE_$it") },
            userRequest.idToken
        )

        val username = oidcUser.idToken.getClaim<String>("username")
        val user = userRepository.findByUsername(username)

        if (user == null) {
            val userInfoAttributes = fetchUserInfo(userRequest)
            createUser(username, userInfoAttributes)
        }

        return oidcUser
    }

    private fun fetchUserInfo(userRequest: OidcUserRequest): Map<String, Any> {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        val body = LinkedMultiValueMap<String, String>().apply {
            add("access_token", userRequest.accessToken.tokenValue)
        }

        val requestEntity = HttpEntity(body, headers)

        val userInfoResponse = restTemplate.exchange<Map<String, Any>>(
            userRequest.clientRegistration.providerDetails.userInfoEndpoint.uri,
            HttpMethod.POST, requestEntity, Map::class.java
        )

        return userInfoResponse.body ?: emptyMap()
    }

    @Transactional
    fun createUser(username: String, userInfo: Map<String, Any>) {

        val name = userInfo["name"] as String
        val email = userInfo["email"] as String

        val groups = userInfo["groups"] as List<String>
        val role = if ("STAFF" in groups) {
            Role.ROLE_STAFF
        } else if ("PROFESSOR" in groups) {
            Role.ROLE_PROFESSOR
        } else if ("GRADUATE" in groups) {
            Role.ROLE_GRADUATE
        } else {
            null
        }

        val newUser = UserEntity(
            username = username,
            name = name,
            email = email,
            role = role
        )

        userRepository.save(newUser)
    }
}
