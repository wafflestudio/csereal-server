package com.wafflestudio.csereal.common.mockauth

import com.wafflestudio.csereal.core.user.database.Role
import com.wafflestudio.csereal.core.user.database.UserEntity
import com.wafflestudio.csereal.core.user.database.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

//TODO: 정식 릴리즈 후에는 dev 서버에서만 가능하게
@RestController
@RequestMapping("/dev")
class DevAuthController(
    private val authenticationManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val securityContextRepository: SecurityContextRepository
) {

    @GetMapping("/mock-login")
    fun mockLogin(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<Any> {
        val mockUser = userRepository.findByUsername("devUser")
            ?: userRepository.save(UserEntity("devUser", "Mock", "mock@abc.com", "0000-00000", Role.ROLE_STAFF))
        val customPrincipal = CustomPrincipal(mockUser)
        val authenticationToken = UsernamePasswordAuthenticationToken(
            customPrincipal, null, listOf(
                SimpleGrantedAuthority("ROLE_STAFF")
            )
        )

        val authentication = authenticationManager.authenticate(authenticationToken)
        SecurityContextHolder.getContext().authentication = authentication

        securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response)

        request.getSession(true)

        return ResponseEntity.ok().body("Mock user authenticated")
    }
}
