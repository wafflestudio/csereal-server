package com.wafflestudio.csereal.core.research.api

import com.wafflestudio.csereal.core.research.dto.ResearchDto
import com.wafflestudio.csereal.core.research.service.ResearchService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/research")
@RestController
class ResearchController(
    private val researchService: ResearchService
) {
    //research 메인 페이지는 안만드는 걸로 알아서 뺐어요

    // postType: groups, centers
    @PostMapping("/{postType}/{name}")
    fun createResearchDetail(
        @PathVariable postType: String,
        @PathVariable name: String,
        @Valid @RequestBody request: ResearchDto
    ) : ResponseEntity<ResearchDto> {
        return ResponseEntity.ok(researchService.createResearchDetail(postType, name, request))
    }
}