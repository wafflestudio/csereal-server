package com.wafflestudio.csereal.core.research.api.v1

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.research.dto.LabDto
import com.wafflestudio.csereal.core.research.dto.ResearchDto
import com.wafflestudio.csereal.core.research.dto.ResearchGroupResponse
import com.wafflestudio.csereal.core.research.service.LabService
import com.wafflestudio.csereal.core.research.service.LabServiceImpl
import com.wafflestudio.csereal.core.research.service.ResearchSearchService
import com.wafflestudio.csereal.core.research.service.ResearchService
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/api/v1/research")
@RestController("ResearchControllerV1")
@Deprecated(message = "Use V2 API")
class ResearchController(
    private val researchService: ResearchService,
    private val researchSearchService: ResearchSearchService,
    private val labService: LabService,
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

    @GetMapping("/labs")
    fun readAllLabs(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<List<LabDto>> {
        return ResponseEntity.ok(labService.readAllLabs(language))
    }

    @GetMapping("/lab/{labId}")
    fun readLab(
        @PathVariable labId: Long
    ): ResponseEntity<LabDto> {
        return ResponseEntity.ok(labService.readLab(labId))
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
