package com.wafflestudio.csereal.common.controller

import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.SystemErrorCode
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Profile("!prod")
@RestController
class ErrorCodeController {
    @GetMapping("/api/v2/errors")
    fun giveErrorInformation(): ResponseEntity<Any> {
        val errors = ErrorCode.values().map {
            mapOf("status" to it.status, "code" to it.code, "msg" to it.msg)
        }
        val systemErrors = SystemErrorCode.values().map {
            mapOf("status" to it.status, "code" to it.code, "msg" to it.msg)
        }
        return ResponseEntity.ok(errors + systemErrors)
    }
}
