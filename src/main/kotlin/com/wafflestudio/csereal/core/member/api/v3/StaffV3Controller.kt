package com.wafflestudio.csereal.core.member.api.v3

import com.wafflestudio.csereal.core.member.api.req.CreateStaffLanguagesReqBody
import com.wafflestudio.csereal.core.member.api.req.ModifyStaffLanguagesReqBody
import com.wafflestudio.csereal.core.member.dto.SimpleStaffDto
import com.wafflestudio.csereal.core.member.dto.StaffLanguagesDto
import com.wafflestudio.csereal.core.member.service.StaffService
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * v3 행정직원 API — 이중언어 단일 리소스. ProfessorV3Controller와 동일한 패턴
 * (단일-id 수정/삭제 + 파일 파트명 `mainImage` 통일 + DELETE 204).
 */
@RequestMapping("/api/v3/staff")
@RestController
class StaffV3Controller(
    private val staffService: StaffService
) {
    @GetMapping("/{staffId}")
    fun getStaff(
        @PathVariable @Positive
        staffId: Long
    ): StaffLanguagesDto = staffService.getStaffLanguages(staffId)

    @GetMapping
    fun getAllStaff(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): List<SimpleStaffDto> = staffService.getAllStaff(language)

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping(consumes = ["multipart/form-data"])
    fun createStaff(
        @RequestPart("request") request: CreateStaffLanguagesReqBody,
        @RequestPart("mainImage") mainImage: MultipartFile?
    ): StaffLanguagesDto = staffService.createStaffLanguages(request, mainImage)

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{staffId}", consumes = ["multipart/form-data"])
    fun updateStaff(
        @PathVariable @Positive
        staffId: Long,
        @RequestPart("request") request: ModifyStaffLanguagesReqBody,
        @RequestPart("mainImage") mainImage: MultipartFile?
    ): StaffLanguagesDto = staffService.updateStaffById(staffId, request, mainImage)

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/{staffId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteStaff(
        @PathVariable @Positive
        staffId: Long
    ) = staffService.deleteStaffById(staffId)
}
