package com.wafflestudio.csereal.core.member.api.v1

import com.wafflestudio.csereal.core.member.dto.SimpleStaffDto
import com.wafflestudio.csereal.core.member.dto.StaffDto
import com.wafflestudio.csereal.core.member.service.StaffService
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Deprecated(message = "Use v2 API")
@RequestMapping("/api/v1/staff")
@RestController("StaffControllerV1")
class StaffController(
    private val staffService: StaffService
) {
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
}
