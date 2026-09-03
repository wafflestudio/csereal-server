package com.wafflestudio.csereal.global

import com.wafflestudio.csereal.common.mockauth.CustomOidcUser
import com.wafflestudio.csereal.core.user.database.UserEntity
import com.wafflestudio.csereal.core.user.database.UserRepository
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import java.time.Instant

/**
 * 테스트 스레드의 SecurityContext에 [user]를 [authorities]("ROLE_…")로 로그인시킨다.
 * 프로덕션 코드는 인증이 없으면 권한 없음(getCurrentUserRoles)·예외(getLoginUser)로 처리하므로,
 * 로그인 사용자를 전제하는 서비스 경로를 테스트할 땐 이걸 먼저 부른다.
 */
fun authenticateAs(user: UserEntity, vararg authorities: String = arrayOf("ROLE_STAFF")) {
    val granted = authorities.map(::SimpleGrantedAuthority)
    val issuedAt = Instant.now()
    val principal = CustomOidcUser(
        user,
        granted,
        OidcIdToken("test-token", issuedAt, issuedAt.plusSeconds(3600), mapOf("sub" to user.username))
    )
    SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(principal, null, granted)
}

/** [username] 사용자를 없으면 만들어 로그인시키고 그 엔티티를 돌려준다. */
fun authenticateAs(
    userRepository: UserRepository,
    username: String,
    vararg authorities: String = arrayOf("ROLE_STAFF")
): UserEntity {
    val user = userRepository.findByUsername(username)
        ?: userRepository.save(UserEntity(username, username, "$username@example.com", "0000-00000"))
    authenticateAs(user, *authorities)
    return user
}

/** 사용자 엔티티 없이 권한만 심는다 — 역할 판정(getCurrentUserRoles)만 타는 경로용. */
fun authenticateRoles(vararg authorities: String) {
    val granted = authorities.map(::SimpleGrantedAuthority)
    SecurityContextHolder.getContext().authentication =
        UsernamePasswordAuthenticationToken("unit-principal", null, granted)
}

fun clearAuthentication() = SecurityContextHolder.clearContext()
