package com.wafflestudio.csereal.core.search.api.v2

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.search.SearchType
import com.wafflestudio.csereal.core.search.api.res.SearchResBody
import com.wafflestudio.csereal.core.search.service.SearchQueryService
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.hibernate.validator.constraints.Length
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/search")
class SearchController(
    private val searchQueryService: SearchQueryService
) {
    @GetMapping
    fun search(
        @RequestParam(required = true)
        @Length(min = 2)
        @NotBlank
        keyword: String,
        @RequestParam(required = false, defaultValue = "ko")
        language: LanguageType,
        @RequestParam(required = false)
        type: List<SearchType>?,
        @RequestParam(required = false, defaultValue = "1")
        @Positive
        pageNum: Int,
        @RequestParam(required = false, defaultValue = "20")
        @Positive
        pageSize: Int
    ): SearchResBody = searchQueryService.search(keyword, language, type, pageNum, pageSize)
}
