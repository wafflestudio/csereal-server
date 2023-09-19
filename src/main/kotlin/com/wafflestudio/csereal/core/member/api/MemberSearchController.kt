package com.wafflestudio.csereal.core.member.api

import com.wafflestudio.csereal.core.member.service.MemberSearchService
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
        @RequestParam(required = true) number: Int
    ) = memberSearchService.searchTopMember(keyword, number)

    @GetMapping
    fun searchPage(
        @RequestParam(required = true) keyword: String,
        @RequestParam(required = true) pageSize: Int,
        @RequestParam(required = true) pageNum: Int
    ) = memberSearchService.searchMember(keyword, pageSize, pageNum)
}
