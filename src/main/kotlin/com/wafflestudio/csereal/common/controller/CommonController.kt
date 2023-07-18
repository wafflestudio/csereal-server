package com.wafflestudio.csereal.common.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CommonController {
    @GetMapping("/hello_world")
    fun helloWorld(): String {
        return "Hello, world!"
    }
}