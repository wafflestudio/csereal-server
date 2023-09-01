package com.wafflestudio.csereal.core.research.api

import com.wafflestudio.csereal.core.research.dto.LabDto
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
    @PostMapping
    fun createResearchDetail(
        @Valid @RequestBody request: ResearchDto
    ) : ResponseEntity<ResearchDto> {
        return ResponseEntity.ok(researchService.createResearchDetail(request))
    }

    @GetMapping("/groups")
    fun readAllResearchGroups() : ResponseEntity<ResearchGroupResponse> {
        return ResponseEntity.ok(researchService.readAllResearchGroups())
    }

    @GetMapping("/centers")
    fun readAllResearchCenters() : ResponseEntity<List<ResearchDto>> {
        return ResponseEntity.ok(researchService.readAllResearchCenters())
    }

    @PatchMapping("/{researchId}")
    fun updateResearchDetail(
        @PathVariable researchId: Long,
        @Valid @RequestBody request: ResearchDto
    ) : ResponseEntity<ResearchDto> {
        return ResponseEntity.ok(researchService.updateResearchDetail(researchId, request))
    }

    @PostMapping("/lab")
    fun createLab(
        @Valid @RequestPart("request") request: LabDto,
        @RequestPart("pdf") pdf: MultipartFile?
    ) : ResponseEntity<LabDto> {
        return ResponseEntity.ok(researchService.createLab(request, pdf))
    }

    @GetMapping("/labs")
    fun readAllLabs() : ResponseEntity<List<LabDto>> {
        return ResponseEntity.ok(researchService.readAllLabs())
    }

    @GetMapping("/lab/{labId}")
    fun readLab(
        @PathVariable labId: Long,
    ) : ResponseEntity<LabDto> {
        return ResponseEntity.ok(researchService.readLab(labId))
    }
}