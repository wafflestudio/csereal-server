package com.wafflestudio.csereal.core.research.api.v3

import com.wafflestudio.csereal.core.research.api.req.CreateLabLanguageReqBody
import com.wafflestudio.csereal.core.research.api.req.CreateResearchLanguageReqBody
import com.wafflestudio.csereal.core.research.api.req.ModifyLabLanguageReqBody
import com.wafflestudio.csereal.core.research.api.req.ModifyResearchLanguageReqBody
import com.wafflestudio.csereal.core.research.dto.LabDto
import com.wafflestudio.csereal.core.research.dto.LabLanguageDto
import com.wafflestudio.csereal.core.research.dto.ResearchLanguageDto
import com.wafflestudio.csereal.core.research.service.LabService
import com.wafflestudio.csereal.core.research.service.ResearchService
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * v3 연구(연구실·연구그룹·연구센터) API — 이중언어 콘텐츠를 "단일 리소스"로 다룬다.
 *
 * v2와의 차이: 수정/삭제가 dual-id(`/{koreanId}/{englishId}`)가 아니라 단일 id다.
 * 한/영 쌍은 서버가 research_language에서 해소하므로, 클라이언트는 목록에서 얻은 아무 언어의
 * id 하나만 있으면 된다(삭제 전 상세조회로 두 id를 캐낼 필요가 없다 — v2 centers/groups의
 * 안티패턴 제거). 파일 파트명도 create·update 모두 `mainImage`로 통일.
 * 읽기/생성은 v2도 이미 단일 id·`{ko, en}` 본문이라 그대로 옮겨온다.
 */
@RequestMapping("/api/v3/research")
@RestController
class ResearchV3Controller(
    private val labService: LabService,
    private val researchService: ResearchService
) {
    // ── 연구그룹·연구센터 (research) ──
    @GetMapping("/{researchId:[0-9]+}")
    fun readResearch(
        @PathVariable @Positive
        researchId: Long
    ): ResearchLanguageDto = researchService.readResearchLanguage(researchId)

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping(consumes = ["multipart/form-data"])
    fun createResearch(
        @RequestPart("request") request: CreateResearchLanguageReqBody,
        @RequestPart("mainImage") mainImage: MultipartFile?
    ): ResearchLanguageDto = researchService.createResearchLanguage(request, mainImage)

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{researchId:[0-9]+}", consumes = ["multipart/form-data"])
    fun updateResearch(
        @PathVariable @Positive
        researchId: Long,
        @RequestPart("request") request: ModifyResearchLanguageReqBody,
        @RequestPart("mainImage") mainImage: MultipartFile?
    ): ResearchLanguageDto = researchService.updateResearchLanguageById(researchId, request, mainImage)

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/{researchId:[0-9]+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteResearch(
        @PathVariable @Positive
        researchId: Long
    ) = researchService.deleteResearchLanguageById(researchId)

    // ── 연구실 (lab) ──
    @GetMapping("/lab")
    fun readAllLabs(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): List<LabDto> = labService.readAllLabs(language)

    @GetMapping("/lab/{labId}")
    fun readLab(
        @PathVariable @Positive
        labId: Long
    ): LabLanguageDto = labService.readLabLanguage(labId)

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/lab", consumes = ["multipart/form-data"])
    fun createLab(
        @Valid
        @RequestPart("request")
        request: CreateLabLanguageReqBody,
        @RequestPart("pdf") pdf: MultipartFile?
    ): LabLanguageDto = labService.createLabLanguage(request, pdf)

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/lab/{labId}", consumes = ["multipart/form-data"])
    fun updateLab(
        @PathVariable @Positive
        labId: Long,
        @Valid
        @RequestPart("request")
        request: ModifyLabLanguageReqBody,
        @RequestPart("pdf") pdf: MultipartFile?
    ): LabLanguageDto = labService.updateLabById(labId, request, pdf)

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/lab/{labId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteLab(
        @PathVariable @Positive
        labId: Long
    ) = labService.deleteLabById(labId)
}
