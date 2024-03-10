package com.wafflestudio.csereal.core.user.api

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.mockauth.CustomPrincipal
import com.wafflestudio.csereal.core.user.dto.StaffAuthResponse
import com.wafflestudio.csereal.core.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/user")
@RestController
class UserController(
    private val userService: UserService
) {

    @GetMapping("/is-staff")
    fun isStaff(authentication: Authentication?): ResponseEntity<StaffAuthResponse> {
        val principal = authentication?.principal ?: throw CserealException.Csereal401("로그인이 필요합니다.")

        val username = when (principal) {
            is OidcUser -> principal.idToken.getClaim("username")
            is CustomPrincipal -> principal.userEntity.username
            else -> throw CserealException.Csereal401("Unsupported principal type")
        }

        return if (userService.checkStaffAuth(username)) {
            ResponseEntity.ok(StaffAuthResponse(true))
        } else {
            ResponseEntity.ok(StaffAuthResponse(false))
        }
    }
}
