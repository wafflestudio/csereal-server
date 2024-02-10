package com.wafflestudio.csereal.core.admissions.api

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.admissions.api.req.AdmissionReqBody
import com.wafflestudio.csereal.core.admissions.dto.AdmissionsDto
import com.wafflestudio.csereal.core.admissions.api.req.AdmissionMigrateElem
import com.wafflestudio.csereal.core.admissions.service.AdmissionsService
import com.wafflestudio.csereal.core.admissions.type.AdmissionsMainType
import com.wafflestudio.csereal.core.admissions.type.AdmissionsPostType
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RequestMapping("/api/v1/admissions")
@RestController
class AdmissionsController(
    private val admissionsService: AdmissionsService
) {
    @AuthenticatedStaff
    @PostMapping("/{mainTypeStr}/{postTypeStr}")
    fun createAdmission(
        @PathVariable(required = true) mainTypeStr: String,
        @PathVariable(required = true) postTypeStr: String,
        @Valid @RequestBody
        req: AdmissionReqBody
    ): AdmissionsDto {
        val mainType = AdmissionsMainType.fromJsonValue(mainTypeStr)
        val postType = AdmissionsPostType.fromJsonValue(postTypeStr)
        return admissionsService.createAdmission(req, mainType, postType)
    }

    @GetMapping
    fun readAdmission(
        @PathVariable(required = true) mainTypeStr: String,
        @PathVariable(required = true) postTypeStr: String,
        @RequestParam(required = true, defaultValue = "ko") language: String
    ): AdmissionsDto {
        val mainType = AdmissionsMainType.fromJsonValue(mainTypeStr)
        val postType = AdmissionsPostType.fromJsonValue(postTypeStr)
        val languageType = LanguageType.makeStringToLanguageType(language)
        return admissionsService.readAdmission(mainType, postType, languageType)
    }

    @PostMapping("/migrate")
    fun migrateAdmissions(
        @RequestBody reqList: List<@Valid AdmissionMigrateElem>
    ): List<AdmissionsDto> = admissionsService.migrateAdmissions(reqList)
}
