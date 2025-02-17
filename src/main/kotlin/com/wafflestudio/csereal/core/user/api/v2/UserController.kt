package com.wafflestudio.csereal.core.user.api.v2

import com.wafflestudio.csereal.common.utils.getCurrentUserRoles
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v2/user")
@RestController
class UserController {

    @GetMapping("/my-role")
    fun getMyRole(): ResponseEntity<Map<String, Any>> {
        val roles = getCurrentUserRoles()
        return ResponseEntity.ok(mapOf("roles" to roles))
    }
}
