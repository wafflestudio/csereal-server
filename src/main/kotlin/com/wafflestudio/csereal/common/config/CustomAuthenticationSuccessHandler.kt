package com.wafflestudio.csereal.common.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler

class CustomAuthenticationSuccessHandler : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val redirectUrl = "http://cse-dev-waffle.bacchus.io:3000/"
        response.sendRedirect(redirectUrl)
    }

}
