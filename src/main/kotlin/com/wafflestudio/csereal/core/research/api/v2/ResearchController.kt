package com.wafflestudio.csereal.core.research.api.v2

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.research.api.req.CreateResearchLanguageReqBody
import com.wafflestudio.csereal.core.research.api.req.ModifyResearchLanguageReqBody
import com.wafflestudio.csereal.core.research.dto.LabDto
import com.wafflestudio.csereal.core.research.dto.ResearchLanguageDto
import com.wafflestudio.csereal.core.research.dto.ResearchSealedDto
import com.wafflestudio.csereal.core.research.service.ResearchSearchService
import com.wafflestudio.csereal.core.research.service.ResearchService
import com.wafflestudio.csereal.core.research.type.ResearchType
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v2/research")
@RestController
class ResearchController(
    private val researchService: ResearchService,
    private val researchSearchService: ResearchSearchService
) {
    @GetMapping("/{researchId:[0-9]+}")
    fun readResearch(
        @Positive
        @PathVariable(required = true)
        researchId: Long
    ): ResearchLanguageDto {
        return researchService.readResearchLanguage(researchId)
    }

    @GetMapping("/{researchType:[a-z A-Z]+}")
    fun readAllResearch(
        @PathVariable(required = true) researchType: String,
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): List<ResearchSealedDto> {
        val researchTypeEnum = ResearchType.fromJsonValue(researchType)
        val languageEnum = LanguageType.makeStringToLanguageType(language)
        return researchService.readAllResearch(languageEnum, researchTypeEnum)
    }

    @AuthenticatedStaff
    @PostMapping(consumes = ["multipart/form-data"])
    fun createResearchGroup(
        @RequestPart("request") request: CreateResearchLanguageReqBody,
        @RequestPart("mainImage") mainImage: MultipartFile?
    ): ResearchLanguageDto = researchService.createResearchLanguage(request, mainImage)

    @AuthenticatedStaff
    @PutMapping("/{koreanId}/{englishId}", consumes = ["multipart/form-data"])
    fun updateResearch(
        @PathVariable @Positive
        koreanId: Long,
        @PathVariable @Positive
        englishId: Long,
        @RequestPart("request") request: ModifyResearchLanguageReqBody,

        @Parameter(description = "image 교체할 경우 업로드. Request Body의 removeImage 관계없이 변경됨.")
        @RequestPart("newMainImage")
        newMainImage: MultipartFile?
    ): ResearchLanguageDto {
        return researchService.updateResearchLanguage(koreanId, englishId, request, newMainImage)
    }

    @AuthenticatedStaff
    @DeleteMapping("/{koreanId}/{englishId}")
    fun deleteResearch(
        @PathVariable @Positive
        koreanId: Long,
        @PathVariable @Positive
        englishId: Long
    ) {
        researchService.deleteResearchLanguage(koreanId, englishId)
    }

    // TODO: Change to Language Unified API
    @GetMapping("/labs")
    fun readAllLabs(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<List<LabDto>> {
        return ResponseEntity.ok(researchService.readAllLabs(language))
    }

    // TODO: Change to Language Unified API
    @GetMapping("/lab/{labId}")
    fun readLab(
        @PathVariable labId: Long
    ): ResponseEntity<LabDto> {
        return ResponseEntity.ok(researchService.readLab(labId))
    }

//    @AuthenticatedStaff
//    @PostMapping("/lab")
//    fun createLab(
//        @Valid
//        @RequestPart("request")
//        request: LabDto,
//        @RequestPart("pdf") pdf: MultipartFile?
//    ): ResponseEntity<LabDto> {
//        return ResponseEntity.ok(researchService.createLab(request, pdf))
//    }
//
//
//    // TODO: Change to Language Unified API
//    @AuthenticatedStaff
//    @PatchMapping("/lab/{labId}")
//    fun updateLab(
//        @PathVariable labId: Long,
//        @Valid
//        @RequestPart("request")
//        request: LabUpdateRequest,
//        @RequestPart("pdf") pdf: MultipartFile?
//    ): ResponseEntity<LabDto> {
//        return ResponseEntity.ok(researchService.updateLab(labId, request, pdf))
//    }

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
