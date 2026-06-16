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

/**
 * v3 통합 검색 — 단일 엔드포인트로 전 섹션을 서버에서 집계한다.
 *
 * 이전엔 프론트가 도메인별 search/top·totalSearch 8개로 팬아웃해 직접 집계했다.
 * 이제 한 번 호출하면 끝. sections로 원하는 섹션만 받는다(태그 필터 대응).
 * sections: about | notice | news | seminar | member | research | admissions | academics
 */
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
