package com.wafflestudio.csereal.core.admin.api

import com.wafflestudio.csereal.core.admin.dto.ImportantResponse
import com.wafflestudio.csereal.core.admin.dto.NewsIdListRequest
import com.wafflestudio.csereal.core.admin.dto.SlideResponse
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
    @GetMapping("/slide")
    fun readAllSlides(
        @RequestParam(required = false, defaultValue = "0") pageNum: Long
    ): ResponseEntity<List<SlideResponse>> {
        return ResponseEntity.ok(adminService.readAllSlides(pageNum))
    }

    @PatchMapping
    fun unSlideManyNews(
        @RequestBody request: NewsIdListRequest
    ) {
        adminService.unSlideManyNews(request.idList)
    }

    @GetMapping("/important")
    fun readAllImportants(
        @RequestParam(required = false, defaultValue = "0") pageNum: Long
    ): ResponseEntity<List<ImportantResponse>> {
        return ResponseEntity.ok(adminService.readAllImportants(pageNum))
    }

}