package com.wafflestudio.csereal.core.research.api

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.research.dto.LabDto
import com.wafflestudio.csereal.core.research.dto.LabUpdateRequest
import com.wafflestudio.csereal.core.research.dto.ResearchDto
import com.wafflestudio.csereal.core.research.dto.ResearchGroupResponse
import com.wafflestudio.csereal.core.research.service.ResearchService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v1/research")
@RestController
class ResearchController(
    private val researchService: ResearchService
) {
    @AuthenticatedStaff
    @PostMapping
    fun createResearchDetail(
        @Valid @RequestPart("request") request: ResearchDto,
        @RequestPart("mainImage") mainImage: MultipartFile?,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ): ResponseEntity<ResearchDto> {
        return ResponseEntity.ok(researchService.createResearchDetail(request, mainImage, attachments))
    }

    @GetMapping("/groups")
    fun readAllResearchGroups(): ResponseEntity<ResearchGroupResponse> {
        return ResponseEntity.ok(researchService.readAllResearchGroups())
    }

    @GetMapping("/centers")
    fun readAllResearchCenters(): ResponseEntity<List<ResearchDto>> {
        return ResponseEntity.ok(researchService.readAllResearchCenters())
    }

    @AuthenticatedStaff
    @PatchMapping("/{researchId}")
    fun updateResearchDetail(
        @PathVariable researchId: Long,
        @Valid @RequestPart("request") request: ResearchDto,
        @RequestPart("mainImage") mainImage: MultipartFile?,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ): ResponseEntity<ResearchDto> {
        return ResponseEntity.ok(researchService.updateResearchDetail(researchId, request, mainImage, attachments))
    }

    @AuthenticatedStaff
    @PostMapping("/lab")
    fun createLab(
        @Valid @RequestPart("request") request: LabDto,
        @RequestPart("pdf") pdf: MultipartFile?
    ): ResponseEntity<LabDto> {
        return ResponseEntity.ok(researchService.createLab(request, pdf))
    }

    @GetMapping("/labs")
    fun readAllLabs(): ResponseEntity<List<LabDto>> {
        return ResponseEntity.ok(researchService.readAllLabs())
    }

    @GetMapping("/lab/{labId}")
    fun readLab(
        @PathVariable labId: Long,
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
            @Valid @RequestPart("request") request: LabUpdateRequest,
            @RequestPart("pdf") pdf: MultipartFile?
    ): ResponseEntity<LabDto> {
        return ResponseEntity.ok(researchService.updateLab(labId, request, pdf))
    }

    @PostMapping("/migrate")
    fun migrateResearchDetail(
        @RequestBody requestList: List<ResearchDto>
    ): ResponseEntity<List<ResearchDto>> {
        return ResponseEntity.ok(researchService.migrateResearchDetail(requestList))
    }
    @PostMapping("/lab/migrate")
    fun migrateLabs(
        @RequestBody requestList: List<LabDto>
    ): ResponseEntity<List<LabDto>> {
        return ResponseEntity.ok(researchService.migrateLabs(requestList))
    }
}
