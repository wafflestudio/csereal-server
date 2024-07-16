package com.wafflestudio.csereal.core.member.api

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.member.api.req.CreateStaffReqBody
import com.wafflestudio.csereal.core.member.api.req.ModifyStaffReqBody
import com.wafflestudio.csereal.core.member.dto.SimpleStaffDto
import com.wafflestudio.csereal.core.member.dto.StaffDto
import com.wafflestudio.csereal.core.member.service.StaffService
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v1/staff")
@RestController
class StaffController(
    private val staffService: StaffService
) {

    @AuthenticatedStaff
    @PostMapping
    fun createStaff(
        @RequestPart("request") createStaffRequest: CreateStaffReqBody,
        @RequestPart("image") image: MultipartFile?
    ): StaffDto =
        staffService.createStaff(createStaffRequest, image)

    @GetMapping("/{staffId}")
    fun getStaff(
        @PathVariable @Positive
        staffId: Long
    ): ResponseEntity<StaffDto> {
        return ResponseEntity.ok(staffService.getStaff(staffId))
    }

    @GetMapping
    fun getAllStaff(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<List<SimpleStaffDto>> {
        return ResponseEntity.ok(staffService.getAllStaff(language))
    }

    // swagger explanation
    @AuthenticatedStaff
    @PutMapping("/{staffId}")
    fun updateStaff(
        @PathVariable @Positive
        staffId: Long,
        @RequestPart("request") modifyStaffReq: ModifyStaffReqBody,
        @Parameter(description = "image 교체할 경우 업로드. Request Body의 removeImage 관계없이 변경됨.")
        @RequestPart("newImage")
        newImage: MultipartFile?
    ): StaffDto =
        staffService.updateStaff(staffId, modifyStaffReq, newImage)

    @AuthenticatedStaff
    @DeleteMapping("/{staffId}")
    fun deleteStaff(
        @PathVariable @Positive
        staffId: Long
    ): ResponseEntity<Any> {
        staffService.deleteStaff(staffId)
        return ResponseEntity.ok().build()
    }
}
