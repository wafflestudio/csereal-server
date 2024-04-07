package com.wafflestudio.csereal.core.member.api

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.member.dto.SimpleStaffDto
import com.wafflestudio.csereal.core.member.dto.StaffDto
import com.wafflestudio.csereal.core.member.service.StaffService
import org.springframework.context.annotation.Profile
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
        @RequestPart("request") createStaffRequest: StaffDto,
        @RequestPart("mainImage") mainImage: MultipartFile?
    ): ResponseEntity<StaffDto> {
        return ResponseEntity.ok(staffService.createStaff(createStaffRequest, mainImage))
    }

    @GetMapping("/{staffId}")
    fun getStaff(@PathVariable staffId: Long): ResponseEntity<StaffDto> {
        return ResponseEntity.ok(staffService.getStaff(staffId))
    }

    @GetMapping
    fun getAllStaff(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<List<SimpleStaffDto>> {
        return ResponseEntity.ok(staffService.getAllStaff(language))
    }

    @AuthenticatedStaff
    fun updateStaff(
        @PathVariable staffId: Long,
        @RequestPart("request") updateStaffRequest: StaffDto,
        @RequestPart("mainImage") mainImage: MultipartFile?
    ): ResponseEntity<StaffDto> {
        return ResponseEntity.ok(staffService.updateStaff(staffId, updateStaffRequest, mainImage))
    }

    @AuthenticatedStaff
    @DeleteMapping("/{staffId}")
    fun deleteStaff(@PathVariable staffId: Long): ResponseEntity<Any> {
        staffService.deleteStaff(staffId)
        return ResponseEntity.ok().build()
    }

    @Profile("!prod")
    @PostMapping("/migrate")
    fun migrateStaff(
        @RequestBody requestList: List<StaffDto>
    ): ResponseEntity<List<StaffDto>> {
        return ResponseEntity.ok(staffService.migrateStaff(requestList))
    }

    @Profile("!prod")
    @PatchMapping("/migrateImage/{staffId}")
    fun migrateStaffImage(
        @PathVariable staffId: Long,
        @RequestPart("mainImage") mainImage: MultipartFile
    ): ResponseEntity<StaffDto> {
        return ResponseEntity.ok(staffService.migrateStaffImage(staffId, mainImage))
    }
}
