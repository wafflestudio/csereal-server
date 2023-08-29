package com.wafflestudio.csereal.core.member.api

import com.wafflestudio.csereal.core.member.dto.SimpleStaffDto
import com.wafflestudio.csereal.core.member.dto.StaffDto
import com.wafflestudio.csereal.core.member.service.StaffService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/staff")
@RestController
class StaffController(
    private val staffService: StaffService
) {

    @PostMapping
    fun createStaff(
        @RequestPart("request") createStaffRequest: StaffDto,
        @RequestPart("image") image: MultipartFile?,
    ): ResponseEntity<StaffDto> {
        return ResponseEntity.ok(staffService.createStaff(createStaffRequest,image))
    }

    @GetMapping("/{staffId}")
    fun getStaff(@PathVariable staffId: Long): ResponseEntity<StaffDto> {
        return ResponseEntity.ok(staffService.getStaff(staffId))
    }

    @GetMapping
    fun getAllStaff(): ResponseEntity<List<SimpleStaffDto>> {
        return ResponseEntity.ok(staffService.getAllStaff())
    }

    @PatchMapping("/{staffId}")
    fun updateStaff(@PathVariable staffId: Long, @RequestBody updateStaffRequest: StaffDto): ResponseEntity<StaffDto> {
        return ResponseEntity.ok(staffService.updateStaff(staffId, updateStaffRequest))
    }

    @DeleteMapping("/{staffId}")
    fun deleteStaff(@PathVariable staffId: Long): ResponseEntity<Any> {
        staffService.deleteStaff(staffId)
        return ResponseEntity.ok().build()
    }
}
