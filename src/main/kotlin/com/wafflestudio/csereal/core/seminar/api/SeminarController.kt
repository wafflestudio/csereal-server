package com.wafflestudio.csereal.core.seminar.api

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.common.enums.ContentSearchSortType
import com.wafflestudio.csereal.common.utils.getUsername
import com.wafflestudio.csereal.core.seminar.dto.SeminarDto
import com.wafflestudio.csereal.core.seminar.dto.SeminarSearchResponse
import com.wafflestudio.csereal.core.seminar.service.SeminarService
import com.wafflestudio.csereal.core.user.database.Role
import com.wafflestudio.csereal.core.user.database.UserRepository
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v1/seminar")
@RestController
class SeminarController(
    private val seminarService: SeminarService,
    private val userRepository: UserRepository
) {
    @GetMapping
    fun searchSeminar(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) pageNum: Int?,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int,
        @RequestParam(required = false, defaultValue = "DATE") sortBy: String,
        authentication: Authentication?
    ): ResponseEntity<SeminarSearchResponse> {
        val username = getUsername(authentication)
        val isStaff = username?.let {
            val user = userRepository.findByUsername(it)
            user?.role == Role.ROLE_STAFF
        } ?: false

        val usePageBtn = pageNum != null
        val page = pageNum ?: 1
        val pageRequest = PageRequest.of(page - 1, pageSize)

        val sortType = ContentSearchSortType.fromJsonValue(sortBy)

        return ResponseEntity.ok(seminarService.searchSeminar(keyword, pageRequest, usePageBtn, sortType, isStaff))
    }

    @AuthenticatedStaff
    @PostMapping
    fun createSeminar(
        @Valid
        @RequestPart("request")
        request: SeminarDto,
        @RequestPart("mainImage") mainImage: MultipartFile?,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ): ResponseEntity<SeminarDto> {
        return ResponseEntity.ok(seminarService.createSeminar(request, mainImage, attachments))
    }

    @GetMapping("/{seminarId}")
    fun readSeminar(
        @PathVariable seminarId: Long,
        authentication: Authentication?
    ): ResponseEntity<SeminarDto> {
        val username = getUsername(authentication)
        val isStaff = username?.let {
            val user = userRepository.findByUsername(it)
            user?.role == Role.ROLE_STAFF
        } ?: false
        return ResponseEntity.ok(seminarService.readSeminar(seminarId, isStaff))
    }

    @AuthenticatedStaff
    @PatchMapping("/{seminarId}")
    fun updateSeminar(
        @PathVariable seminarId: Long,
        @Valid
        @RequestPart("request")
        request: SeminarDto,
        @RequestPart("newMainImage") newMainImage: MultipartFile?,
        @RequestPart("newAttachments") newAttachments: List<MultipartFile>?
    ): ResponseEntity<SeminarDto> {
        return ResponseEntity.ok(
            seminarService.updateSeminar(
                seminarId,
                request,
                newMainImage,
                newAttachments
            )
        )
    }

    @AuthenticatedStaff
    @DeleteMapping("/{seminarId}")
    fun deleteSeminar(
        @PathVariable seminarId: Long
    ) {
        seminarService.deleteSeminar(seminarId)
    }

    @GetMapping("/ids")
    fun getAllIds(): ResponseEntity<List<Long>> {
        return ResponseEntity.ok(seminarService.getAllIds())
    }
}
