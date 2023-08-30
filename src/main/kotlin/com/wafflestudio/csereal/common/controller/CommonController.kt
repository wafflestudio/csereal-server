package com.wafflestudio.csereal.common.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController("/api/v1")
class CommonController {
    @GetMapping("/helloworld")
    fun helloWorld(): String {
        return "Hello, world!"
    }
}