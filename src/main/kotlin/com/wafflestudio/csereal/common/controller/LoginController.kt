package com.wafflestudio.csereal.common.controller

import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Profile("!test")
@RestController
class LoginController(
    @Value("\${login-page}")
    private val loginPage: String
) {
    @GetMapping("/api/v1/login")
    fun redirectToLoginURL(response: HttpServletResponse) {
        val redirectUrl = "$loginPage/oauth2/authorization/idsnucse"
        response.sendRedirect(redirectUrl)
    }
}
