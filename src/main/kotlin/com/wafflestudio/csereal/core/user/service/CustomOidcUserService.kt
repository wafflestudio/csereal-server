package com.wafflestudio.csereal.core.user.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.mockauth.CustomOidcUser
import com.wafflestudio.csereal.core.user.database.UserEntity
import com.wafflestudio.csereal.core.user.database.UserRepository
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.core.GrantedAuthority
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

    @Transactional
    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
        val oidcUser = DefaultOidcUser(
            userRequest.clientRegistration.scopes.map { SimpleGrantedAuthority("SCOPE_$it") },
            userRequest.idToken
        )

        val username = oidcUser.idToken.getClaim<String>("username")
        var user = userRepository.findByUsername(username)

        if (user == null) {
            val userInfoAttributes = fetchUserInfo(userRequest)
            user = createUser(username, userInfoAttributes)
        }

        val authorities = mutableSetOf<GrantedAuthority>()
        authorities.addAll(oidcUser.authorities)

        val groups = oidcUser.idToken.getClaim<List<String>>("groups") ?: emptyList()
        if ("staff" in groups) {
            authorities.add(SimpleGrantedAuthority("ROLE_STAFF"))
        }
        if ("professor" in groups || "graduate" in groups) {
            authorities.add(SimpleGrantedAuthority("ROLE_RESERVATION"))
        }
        if ("student-council" in groups) {
            authorities.add(SimpleGrantedAuthority("ROLE_COUNCIL"))
        }

        return CustomOidcUser(user, authorities, oidcUser.idToken)
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
            HttpMethod.POST,
            requestEntity,
            Map::class.java
        )

        if (userInfoResponse.body?.get("sub") != userRequest.idToken.getClaim("sub")) {
            throw CserealException.Csereal401("Authentication failed")
        }

        return userInfoResponse.body ?: emptyMap()
    }

    private fun createUser(username: String, userInfo: Map<String, Any>): UserEntity {
        val name = userInfo["name"] as String
        val email = userInfo["email"] as String
        val studentId = userInfo["student_id"] as String

        val newUser = UserEntity(
            username = username,
            name = name,
            email = email,
            studentId = studentId
        )

        userRepository.save(newUser)

        return newUser
    }
}
