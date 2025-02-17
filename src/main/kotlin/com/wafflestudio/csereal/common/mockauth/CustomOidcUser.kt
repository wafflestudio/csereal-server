package com.wafflestudio.csereal.common.mockauth

import com.wafflestudio.csereal.core.user.database.UserEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.OidcUser

data class CustomOidcUser(
    val userEntity: UserEntity,
    private val authorities: Collection<GrantedAuthority>,
    private val idToken: OidcIdToken,
    private val userInfo: OidcUserInfo? = null
) : OidcUser, UserDetails {
    override fun getName(): String = idToken.subject
    override fun getAttributes(): MutableMap<String, Any> = idToken.claims.toMutableMap()
    override fun getAuthorities(): Collection<GrantedAuthority> = authorities
    override fun getClaims(): Map<String, Any> = idToken.claims
    override fun getUserInfo(): OidcUserInfo? = userInfo
    override fun getIdToken(): OidcIdToken = idToken

    override fun getPassword(): String? = null
    override fun getUsername(): String = userEntity.username
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}
