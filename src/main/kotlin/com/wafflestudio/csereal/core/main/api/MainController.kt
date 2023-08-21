package com.wafflestudio.csereal.core.main.api

import com.wafflestudio.csereal.core.main.dto.MainResponse
import com.wafflestudio.csereal.core.main.service.MainService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping
@RestController
class MainController(
    private val mainService: MainService,
) {
    @GetMapping
    fun readMain() : MainResponse {
        return mainService.readMain()
    }
}