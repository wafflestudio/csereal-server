package com.wafflestudio.csereal.core.user.api

import com.wafflestudio.csereal.core.user.dto.LoginRequest
import com.wafflestudio.csereal.core.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(private val userService: UserService) {

    @PostMapping("/login")
    fun login(loginRequest: LoginRequest): ResponseEntity<Any> {
        userService.login(loginRequest)
        return ResponseEntity.ok().build()
    }
}
