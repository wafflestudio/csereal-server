package com.wafflestudio.csereal.core.admissions.api.v2

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.admissions.api.req.UpdateAdmissionReq
import com.wafflestudio.csereal.core.admissions.api.res.GroupedAdmission
import com.wafflestudio.csereal.core.admissions.service.AdmissionsService
import com.wafflestudio.csereal.core.admissions.type.AdmissionsMainType
import com.wafflestudio.csereal.core.admissions.type.AdmissionsPostType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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

    @AuthenticatedStaff
    @PutMapping("/{mainTypeStr}/{postTypeStr}")
    fun updateAdmission(
        @PathVariable(required = true) mainTypeStr: String,
        @PathVariable(required = true) postTypeStr: String,
        @RequestBody updateAdmissionReq: UpdateAdmissionReq
    ) {
        val mainType = AdmissionsMainType.fromJsonValue(mainTypeStr)
        val postType = AdmissionsPostType.fromJsonValue(postTypeStr)
        admissionsService.updateAdmission(mainType, postType, updateAdmissionReq)
    }
}
