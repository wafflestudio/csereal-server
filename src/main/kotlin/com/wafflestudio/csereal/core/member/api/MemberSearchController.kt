package com.wafflestudio.csereal.core.member.api

import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.member.service.MemberSearchService
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/member/search")
class MemberSearchController(
    private val memberSearchService: MemberSearchService
) {
    @GetMapping("/top")
    fun searchTop(
        @RequestParam(required = true) keyword: String,
        @RequestParam(required = true) @Valid @Positive number: Int,
        @RequestParam(required = true, defaultValue = "ko") language: String
    ) = LanguageType.makeStringToLanguageType(language).let {
        memberSearchService.searchTopMember(keyword, it, number)
    }

    @GetMapping
    fun searchPage(
        @RequestParam(required = true) keyword: String,
        @RequestParam(required = true, defaultValue = "ko") language: String,
        @RequestParam(required = true) @Valid @Positive pageSize: Int,
        @RequestParam(required = true) @Valid @Positive pageNum: Int
    ) = LanguageType.makeStringToLanguageType(language).let {
        memberSearchService.searchMember(keyword, it, pageSize, pageNum)
    }
}
