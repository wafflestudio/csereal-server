package com.wafflestudio.csereal.core.user.api

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.user.dto.StaffAuthResponse
import com.wafflestudio.csereal.core.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
    fun isStaff(@AuthenticationPrincipal oidcUser: OidcUser?): ResponseEntity<StaffAuthResponse> {
        if (oidcUser == null) {
            throw CserealException.Csereal401("로그인이 필요합니다.")
        }
        val username = oidcUser.idToken.getClaim<String>("username")
        if (userService.checkStaffAuth(username)) {
            return ResponseEntity.ok(StaffAuthResponse(true))
        } else {
            return ResponseEntity.ok(StaffAuthResponse(false))
        }
    }
}
