package com.wafflestudio.csereal.core.admissions.api.v2

import com.wafflestudio.csereal.core.admissions.api.req.UpdateAdmissionReq
import com.wafflestudio.csereal.core.admissions.api.res.GroupedAdmission
import com.wafflestudio.csereal.core.admissions.service.AdmissionsService
import com.wafflestudio.csereal.core.admissions.type.AdmissionsMainType
import com.wafflestudio.csereal.core.admissions.type.AdmissionsPostType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RequestMapping("/api/v2/admissions")
@RestController
class AdmissionsController(
    private val admissionsService: AdmissionsService
) {
    @GetMapping("/{mainType}/{postType}")
    fun readAdmission(
        @PathVariable(required = true) mainType: AdmissionsMainType,
        @PathVariable(required = true) postType: AdmissionsPostType
    ): GroupedAdmission {
        return admissionsService.readGroupedAdmission(mainType, postType)
    }

    // TODO: Add Create, Delete Admission Pair API if needed

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{mainType}/{postType}")
    fun updateAdmission(
        @PathVariable(required = true) mainType: AdmissionsMainType,
        @PathVariable(required = true) postType: AdmissionsPostType,
        @RequestBody updateAdmissionReq: UpdateAdmissionReq
    ) {
        admissionsService.updateGroupedAdmission(mainType, postType, updateAdmissionReq)
    }
}
