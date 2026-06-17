package com.wafflestudio.csereal.core.main.api.v3

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.main.dto.SearchV3Response
import com.wafflestudio.csereal.core.main.service.MainService
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.hibernate.validator.constraints.Length
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

// v3 통합 검색: 단일 엔드포인트로 여러 섹션을 서버에서 집계(기존엔 도메인별로 여러 번 호출).
// sections로 일부만 지정 가능, 비우면 전체. (about|notice|news|seminar|member|research|admissions|academics)
@RequestMapping("/api/v3/search")
@RestController
class SearchV3Controller(
    private val mainService: MainService
) {
    @GetMapping
    fun search(
        @RequestParam(required = true)
        @Length(min = 2)
        @NotBlank
        keyword: String,
        @RequestParam(required = false) sections: Set<String>?,
        @RequestParam(required = false, defaultValue = "3") @Positive number: Int,
        @RequestParam(required = false, defaultValue = "10") @Positive memberNumber: Int,
        @RequestParam(required = false, defaultValue = "200") @Positive stringLength: Int,
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): SearchV3Response = mainService.searchV3(
        keyword,
        sections,
        number,
        memberNumber,
        stringLength,
        LanguageType.makeStringToLanguageType(language)
    )
}
