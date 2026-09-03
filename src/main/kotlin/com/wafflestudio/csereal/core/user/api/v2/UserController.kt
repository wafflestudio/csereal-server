package com.wafflestudio.csereal.core.user.api.v2

import com.wafflestudio.csereal.common.utils.getCurrentUserRoles
import com.wafflestudio.csereal.core.user.RoleType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v2/user")
@RestController
class UserController {

    @GetMapping("/my-role")
    fun getMyRole(): ResponseEntity<MyRoleResponse> {
        return ResponseEntity.ok(MyRoleResponse(getCurrentUserRoles().toList()))
    }
}

data class MyRoleResponse(
    val roles: List<RoleType>
)
