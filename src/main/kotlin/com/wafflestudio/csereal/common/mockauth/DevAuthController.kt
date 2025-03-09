package com.wafflestudio.csereal.common.mockauth

import com.wafflestudio.csereal.core.user.database.UserEntity
import com.wafflestudio.csereal.core.user.database.UserRepository
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@Profile("!prod")
@RestController
@RequestMapping("/api/v2")
class DevAuthController(
    private val securityContextRepository: SecurityContextRepository,
    private val userRepository: UserRepository
) {

    @GetMapping("/mock-login")
    fun mockLogin(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @RequestParam(defaultValue = "ROLE_STAFF") role: String
    ): ResponseEntity<String> {
        val mockUser = userRepository.findByUsername("devUser")
            ?: userRepository.save(UserEntity("devUser", "Mock", "mock@abc.com", "0000-00000"))

        val authorities = listOf(SimpleGrantedAuthority(role))

        // dummy token creation
        val issuedAt = Instant.now()
        val expiresAt = issuedAt.plusSeconds(3600)
        val claims = mapOf("sub" to mockUser.username)
        val dummyIdToken = OidcIdToken("mock-token", issuedAt, expiresAt, claims)

        val customOidcUser = CustomOidcUser(mockUser, authorities, dummyIdToken)
        val authentication = UsernamePasswordAuthenticationToken(customOidcUser, null, authorities)

        SecurityContextHolder.getContext().authentication = authentication
        securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response)

        return ResponseEntity.ok("Mock login successful with role: $role")
    }

    @GetMapping("/mock-logout")
    fun mockLogout(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<String> {
        request.getSession(false)?.invalidate()
        val cookie = Cookie("JSESSIONID", null).apply {
            path = "/"
            maxAge = 0
        }
        response.addCookie(cookie)
        return ResponseEntity.ok("Mock logout successful")
    }
}
