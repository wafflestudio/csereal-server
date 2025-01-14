package com.wafflestudio.csereal.core.admin.api.v2

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.admin.dto.*
import com.wafflestudio.csereal.core.admin.service.AdminService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v2/admin")
@RestController
class AdminController(
    private val adminService: AdminService
) {
    @AuthenticatedStaff
    @GetMapping("/slide")
    fun readAllSlides(
        @RequestParam(required = false, defaultValue = "1") pageNum: Long,
        @RequestParam(required = false, defaultValue = "40") pageSize: Int
    ): ResponseEntity<AdminSlidesResponse> {
        return ResponseEntity.ok(adminService.readAllSlides(pageNum - 1, pageSize))
    }

    @AuthenticatedStaff
    @PatchMapping("/slide")
    fun unSlideManyNews(
        @RequestBody request: NewsIdListRequest
    ) {
        adminService.unSlideManyNews(request.newsIdList)
    }

    @AuthenticatedStaff
    @GetMapping("/important")
    fun readAllImportants(
        @RequestParam(required = false, defaultValue = "1") pageNum: Int,
        @RequestParam(required = false, defaultValue = "40") pageSize: Int
    ): ResponseEntity<AdminImportantResponse> {
        return ResponseEntity.ok(
            adminService.readAllImportants(pageNum - 1, pageSize)
        )
    }

    @AuthenticatedStaff
    @PatchMapping("/important")
    fun makeNotImportants(
        @RequestBody request: ImportantRequest
    ) {
        adminService.makeNotImportants(request.targetInfos)
    }
}
