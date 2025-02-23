package com.wafflestudio.csereal.core.admissions.api.v2

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.admissions.api.req.UpdateAdmissionReq
import com.wafflestudio.csereal.core.admissions.api.res.GroupedAdmission
import com.wafflestudio.csereal.core.admissions.service.AdmissionsService
import com.wafflestudio.csereal.core.admissions.type.AdmissionsMainType
import com.wafflestudio.csereal.core.admissions.type.AdmissionsPostType
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RequestMapping("/api/v2/admissions")
@RestController
class AdmissionsController(
    private val admissionsService: AdmissionsService
) {
    @GetMapping("/{mainTypeStr}/{postTypeStr}")
    fun readAdmission(
        @PathVariable(required = true) mainTypeStr: String,
        @PathVariable(required = true) postTypeStr: String
    ): GroupedAdmission {
        val mainType = AdmissionsMainType.fromJsonValue(mainTypeStr)
        val postType = AdmissionsPostType.fromJsonValue(postTypeStr)
        return admissionsService.readGroupedAdmission(mainType, postType)
    }

    // TODO: Add Create, Delete Admission Pair API if needed

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{mainTypeStr}/{postTypeStr}")
    fun updateAdmission(
        @PathVariable(required = true) mainTypeStr: String,
        @PathVariable(required = true) postTypeStr: String,
        @RequestBody updateAdmissionReq: UpdateAdmissionReq
    ) {
        val mainType = AdmissionsMainType.fromJsonValue(mainTypeStr)
        val postType = AdmissionsPostType.fromJsonValue(postTypeStr)
        admissionsService.updateGroupedAdmission(mainType, postType, updateAdmissionReq)
    }

    @GetMapping("/search/top")
    fun searchTopAdmissions(
        @RequestParam(required = true) keyword: String,
        @RequestParam(required = true, defaultValue = "ko") language: String,
        @RequestParam(required = true) @Valid @Positive number: Int,
        @RequestParam(required = false, defaultValue = "30") @Valid @Positive amount: Int
    ) = admissionsService.searchTopAdmission(
        keyword,
        LanguageType.makeStringToLanguageType(language),
        number,
        amount
    )

    @GetMapping("/search")
    fun searchPageAdmissions(
        @RequestParam(required = true) keyword: String,
        @RequestParam(required = true, defaultValue = "ko") language: String,
        @RequestParam(required = true) @Valid @Positive pageSize: Int,
        @RequestParam(required = true) @Valid @Positive pageNum: Int,
        @RequestParam(required = false, defaultValue = "30") @Valid @Positive amount: Int
    ) = admissionsService.searchPageAdmission(
        keyword,
        LanguageType.makeStringToLanguageType(language),
        pageSize,
        pageNum,
        amount
    )
}
