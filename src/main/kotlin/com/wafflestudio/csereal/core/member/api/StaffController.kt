package com.wafflestudio.csereal.core.member.api

import com.wafflestudio.csereal.core.member.dto.SimpleStaffDto
import com.wafflestudio.csereal.core.member.dto.StaffDto
import com.wafflestudio.csereal.core.member.service.StaffService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v1/staff")
@RestController
class StaffController(
    private val staffService: StaffService
) {

    @PostMapping
    fun createStaff(
        @RequestPart("request") createStaffRequest: StaffDto,
        @RequestPart("mainImage") mainImage: MultipartFile?,
    ): ResponseEntity<StaffDto> {
        return ResponseEntity.ok(staffService.createStaff(createStaffRequest,mainImage))
    }

    @GetMapping("/{staffId}")
    fun getStaff(@PathVariable staffId: Long): ResponseEntity<StaffDto> {
        return ResponseEntity.ok(staffService.getStaff(staffId))
    }

    @GetMapping
    fun getAllStaff(): ResponseEntity<List<SimpleStaffDto>> {
        return ResponseEntity.ok(staffService.getAllStaff())
    }

    fun updateStaff(
        @PathVariable staffId: Long,
        @RequestPart("request") updateStaffRequest: StaffDto,
        @RequestPart("mainImage") mainImage: MultipartFile?,
    ): ResponseEntity<StaffDto> {
        return ResponseEntity.ok(staffService.updateStaff(staffId, updateStaffRequest, mainImage))
    }

    @DeleteMapping("/{staffId}")
    fun deleteStaff(@PathVariable staffId: Long): ResponseEntity<Any> {
        staffService.deleteStaff(staffId)
        return ResponseEntity.ok().build()
    }
}
