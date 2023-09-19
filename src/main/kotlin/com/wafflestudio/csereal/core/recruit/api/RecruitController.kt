package com.wafflestudio.csereal.core.recruit.api

import com.wafflestudio.csereal.core.recruit.dto.RecruitPage
import com.wafflestudio.csereal.core.recruit.service.RecruitService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/recruit")
@RestController
class RecruitController(
    private val recruitService: RecruitService
) {

    @GetMapping
    fun getRecruitPage(): ResponseEntity<RecruitPage> {
        return ResponseEntity.ok(recruitService.getRecruitPage())
    }
}
