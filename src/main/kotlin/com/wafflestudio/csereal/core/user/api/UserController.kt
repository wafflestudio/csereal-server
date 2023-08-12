package com.wafflestudio.csereal.core.user.api

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {

    @GetMapping("/oidc-principal")
    fun getOidcUserPrincipal(@AuthenticationPrincipal principal: OidcUser?): OidcUser? {
        return principal
    }

}
