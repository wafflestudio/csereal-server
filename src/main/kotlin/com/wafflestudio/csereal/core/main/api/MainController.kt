package com.wafflestudio.csereal.core.main.api

import com.wafflestudio.csereal.core.main.dto.MainResponse
import com.wafflestudio.csereal.core.main.service.MainService
import jakarta.validation.constraints.Positive
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1")
@RestController
class MainController(
    private val mainService: MainService
) {
    @GetMapping
    fun readMain(
        @RequestParam(required = false)
        @Positive
        importantCnt: Int?
    ): MainResponse =
        mainService.readMain(importantCnt)

    @GetMapping("/search/refresh")
    fun refreshSearches() {
        mainService.refreshSearch()
    }
}
