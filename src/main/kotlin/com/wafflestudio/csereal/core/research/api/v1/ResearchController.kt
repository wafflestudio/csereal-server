package com.wafflestudio.csereal.core.research.api.v1

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.research.dto.LabDto
import com.wafflestudio.csereal.core.research.dto.LabUpdateRequest
import com.wafflestudio.csereal.core.research.dto.ResearchDto
import com.wafflestudio.csereal.core.research.dto.ResearchGroupResponse
import com.wafflestudio.csereal.core.research.service.ResearchSearchService
import com.wafflestudio.csereal.core.research.service.ResearchService
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v1/research")
@RestController("ResearchControllerV1")
@Deprecated(message = "Use V2 API")
class ResearchController(
    private val researchService: ResearchService,
    private val researchSearchService: ResearchSearchService
) {
    @GetMapping("/groups")
    fun readAllResearchGroups(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<ResearchGroupResponse> {
        return ResponseEntity.ok(researchService.readAllResearchGroupsDeprecated(language))
    }

    @GetMapping("/centers")
    fun readAllResearchCenters(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<List<ResearchDto>> {
        return ResponseEntity.ok(researchService.readAllResearchCentersDeprecated(language))
    }

    @AuthenticatedStaff
    @PostMapping("/lab")
    fun createLab(
        @Valid
        @RequestPart("request")
        request: LabDto,
        @RequestPart("pdf") pdf: MultipartFile?
    ): ResponseEntity<LabDto> {
        return ResponseEntity.ok(researchService.createLab(request, pdf))
    }

    @GetMapping("/labs")
    fun readAllLabs(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<List<LabDto>> {
        return ResponseEntity.ok(researchService.readAllLabs(language))
    }

    @GetMapping("/lab/{labId}")
    fun readLab(
        @PathVariable labId: Long
    ): ResponseEntity<LabDto> {
        return ResponseEntity.ok(researchService.readLab(labId))
    }

    /**
     * Research Group 수정은 일단 제외하였음.
     */
    @AuthenticatedStaff
    @PatchMapping("/lab/{labId}")
    fun updateLab(
        @PathVariable labId: Long,
        @Valid
        @RequestPart("request")
        request: LabUpdateRequest,
        @RequestPart("pdf") pdf: MultipartFile?
    ): ResponseEntity<LabDto> {
        return ResponseEntity.ok(researchService.updateLab(labId, request, pdf))
    }

    @GetMapping("/search/top")
    fun searchTop(
        @RequestParam(required = true) keyword: String,
        @RequestParam(required = true) @Valid @Positive number: Int,
        @RequestParam(required = true, defaultValue = "ko") language: String,
        @RequestParam(required = false, defaultValue = "30") @Valid @Positive amount: Int
    ) = researchSearchService.searchTopResearch(
        keyword,
        LanguageType.makeStringToLanguageType(language),
        number,
        amount
    )

    @GetMapping("/search")
    fun searchPage(
        @RequestParam(required = true) keyword: String,
        @RequestParam(required = true) pageSize: Int,
        @RequestParam(required = true) pageNum: Int,
        @RequestParam(required = true, defaultValue = "ko") language: String,
        @RequestParam(required = false, defaultValue = "30") @Valid @Positive amount: Int
    ) = researchSearchService.searchResearch(
        keyword,
        LanguageType.makeStringToLanguageType(language),
        pageSize,
        pageNum,
        amount
    )
}
