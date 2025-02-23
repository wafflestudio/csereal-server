package com.wafflestudio.csereal.core.recruit.api.v2

import com.wafflestudio.csereal.core.recruit.api.req.ModifyRecruitReqBody
import com.wafflestudio.csereal.core.recruit.dto.RecruitPage
import com.wafflestudio.csereal.core.recruit.service.RecruitService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v2/recruit")
@RestController
class RecruitController(
    private val recruitService: RecruitService
) {

    @GetMapping
    fun getRecruitPage(): ResponseEntity<RecruitPage> {
        return ResponseEntity.ok(recruitService.getRecruitPage())
    }

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping(consumes = ["multipart/form-data"])
    fun upsertRecruitPage(
        @RequestPart("request") modifyRecruitReqBody: ModifyRecruitReqBody,
        @RequestPart("newMainImage") newMainImage: MultipartFile?
    ): RecruitPage {
        return recruitService.upsertRecruitPage(modifyRecruitReqBody, newMainImage)
    }
}
