package com.wafflestudio.csereal.core.admin.api

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

@RequestMapping("/api/v1/admin")
@RestController
class AdminController(
    private val adminService: AdminService
) {
    @AuthenticatedStaff
    @GetMapping("/slide")
    fun readAllSlides(
        @RequestParam(required = false, defaultValue = "0") pageNum: Long
    ): ResponseEntity<List<SlideResponse>> {
        return ResponseEntity.ok(adminService.readAllSlides(pageNum))
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
        @RequestParam(required = false, defaultValue = "0") pageNum: Long
    ): ResponseEntity<List<ImportantResponse>> {
        return ResponseEntity.ok(adminService.readAllImportants(pageNum))
    }

    @AuthenticatedStaff
    @PatchMapping("/important")
    fun makeNotImportants(
        @RequestBody request: ImportantRequest
    ) {
        adminService.makeNotImportants(request.targetInfos)
    }


}
