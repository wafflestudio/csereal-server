package com.wafflestudio.csereal.core.member.api.v2

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.member.api.req.CreateStaffLanguagesReqBody
import com.wafflestudio.csereal.core.member.api.req.ModifyStaffLanguagesReqBody
import com.wafflestudio.csereal.core.member.dto.SimpleStaffDto
import com.wafflestudio.csereal.core.member.dto.StaffLanguagesDto
import com.wafflestudio.csereal.core.member.service.StaffService
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v2/staff")
@RestController
class StaffController(
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
    ): ResponseEntity<List<SimpleStaffDto>> {
        return ResponseEntity.ok(staffService.getAllStaff(language))
    }

    @AuthenticatedStaff
    @PostMapping(consumes = ["multipart/form-data"])
    fun createStaff(
        @RequestPart("request") createStaffLanguagesReqBody: CreateStaffLanguagesReqBody,
        @RequestPart("image") image: MultipartFile?
    ): StaffLanguagesDto = staffService.createStaffLanguages(createStaffLanguagesReqBody, image)

    @AuthenticatedStaff
    @PutMapping("/{koStaffId}/{enStaffId}", consumes = ["multipart/form-data"])
    fun updateStaff(
        @PathVariable @Positive
        koStaffId: Long,
        @PathVariable @Positive
        enStaffId: Long,
        @RequestPart("request") modifyStaffLanguageReq: ModifyStaffLanguagesReqBody,

        @Parameter(description = "image 교체할 경우 업로드. Request Body의 removeImage 관계없이 변경됨.")
        @RequestPart("newImage")
        newImage: MultipartFile?
    ): StaffLanguagesDto =
        staffService.updateStaffLanguages(koStaffId, enStaffId, modifyStaffLanguageReq, newImage)

    @AuthenticatedStaff
    @DeleteMapping("/{koStaffId}/{enStaffId}")
    fun deleteStaff(
        @PathVariable @Positive
        koStaffId: Long,

        @PathVariable @Positive
        enStaffId: Long
    ) {
        staffService.deleteStaffLanguages(koStaffId, enStaffId)
    }
}
