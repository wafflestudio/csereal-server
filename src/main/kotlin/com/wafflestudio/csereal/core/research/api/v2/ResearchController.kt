package com.wafflestudio.csereal.core.research.api.v2

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.research.api.req.CreateLabLanguageReqBody
import com.wafflestudio.csereal.core.research.api.req.CreateResearchLanguageReqBody
import com.wafflestudio.csereal.core.research.api.req.ModifyLabLanguageReqBody
import com.wafflestudio.csereal.core.research.api.req.ModifyResearchLanguageReqBody
import com.wafflestudio.csereal.core.research.dto.*
import com.wafflestudio.csereal.core.research.service.LabService
import com.wafflestudio.csereal.core.research.service.ResearchSearchService
import com.wafflestudio.csereal.core.research.service.ResearchService
import com.wafflestudio.csereal.core.research.type.ResearchType
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v2/research")
@RestController
class ResearchController(
    private val researchService: ResearchService,
    private val labService: LabService,
    private val researchSearchService: ResearchSearchService
) {
    // Research APIs

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

    // Lab APIs

    @GetMapping("/lab")
    fun readAllLabs(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): List<LabDto> = labService.readAllLabs(language)

    // TODO: Change to Language Unified API
    @GetMapping("/lab/{labId}")
    fun readLab(
        @PathVariable labId: Long
    ): LabLanguageDto = labService.readLabLanguage(labId)

    @AuthenticatedStaff
    @PostMapping("/lab", consumes = ["multipart/form-data"])
    fun createLab(
        @Valid
        @RequestPart("request")
        request: CreateLabLanguageReqBody,

        @RequestPart("pdf") pdf: MultipartFile?
    ): LabLanguageDto = labService.createLabLanguage(request, pdf)

    @AuthenticatedStaff
    @PutMapping("/lab/{koreanLabId}/{englishLabId}", consumes = ["multipart/form-data"])
    fun updateLab(
        @PathVariable @Positive
        koreanLabId: Long,
        @PathVariable @Positive
        englishLabId: Long,
        @Valid
        @RequestPart("request")
        request: ModifyLabLanguageReqBody,
        @RequestPart("pdf") pdf: MultipartFile?
    ): LabLanguageDto = labService.updateLabLanguage(koreanLabId, englishLabId, request, pdf)

    @AuthenticatedStaff
    @DeleteMapping("/lab/{koreanLabId}/{englishLabId}")
    fun deleteLab(
        @PathVariable @Positive
        koreanLabId: Long,
        @PathVariable @Positive
        englishLabId: Long
    ) {
        labService.deleteLabLanguage(koreanLabId, englishLabId)
    }

    // Search APIs

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
