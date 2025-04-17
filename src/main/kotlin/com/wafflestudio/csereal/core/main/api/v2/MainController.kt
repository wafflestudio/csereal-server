package com.wafflestudio.csereal.core.main.api.v2

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.main.api.res.TotalSearchResponse
import com.wafflestudio.csereal.core.main.dto.MainResponse
import com.wafflestudio.csereal.core.main.service.MainService
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.hibernate.validator.constraints.Length
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v2")
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

    @GetMapping("/totalSearch")
    fun searchTotal(
        @RequestParam(required = true) @Length(min = 2) @NotBlank keyword: String,
        @RequestParam(required = false, defaultValue = "3") @Positive number: Int,
        @RequestParam(required = false, defaultValue = "10") @Positive memberNumber: Int,
        @RequestParam(required = false, defaultValue = "200") @Positive stringLength: Int,
        @RequestParam(required = false, defaultValue = "ko") language: String,
    ): TotalSearchResponse {
        return mainService.totalSearch(
            keyword,
            number,
            memberNumber,
            stringLength,
            LanguageType.makeStringToLanguageType(language),
        )
    }
}
