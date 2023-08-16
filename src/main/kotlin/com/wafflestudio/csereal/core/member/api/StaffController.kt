package com.wafflestudio.csereal.core.member.api

import com.wafflestudio.csereal.core.member.dto.SimpleStaffDto
import com.wafflestudio.csereal.core.member.dto.StaffDto
import com.wafflestudio.csereal.core.member.service.StaffService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/staff")
@RestController
class StaffController(
    private val staffService: StaffService
) {

    @PostMapping
    fun createStaff(@RequestBody createStaffRequest: StaffDto): ResponseEntity<StaffDto> {
        return ResponseEntity.ok(staffService.createStaff(createStaffRequest))
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
