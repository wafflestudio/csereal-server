package com.wafflestudio.csereal.core.seminar.api

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.seminar.dto.SeminarDto
import com.wafflestudio.csereal.core.seminar.dto.SeminarSearchResponse
import com.wafflestudio.csereal.core.seminar.service.SeminarService
import com.wafflestudio.csereal.core.user.database.Role
import com.wafflestudio.csereal.core.user.database.UserRepository
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
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
        @RequestParam(required = false) pageSize: Int?,
        @AuthenticationPrincipal oidcUser: OidcUser?
    ): ResponseEntity<SeminarSearchResponse> {
        val isStaff = oidcUser?.let {
            val username = it.idToken.getClaim<String>("username")
            val user = userRepository.findByUsername(username)
            user?.role == Role.ROLE_STAFF
        } ?: false

        val pgSize = pageSize ?: 10
        val usePageBtn = pageNum != null
        val page = pageNum ?: 1
        val pageRequest = PageRequest.of(page - 1, pgSize)
        return ResponseEntity.ok(seminarService.searchSeminar(keyword, pageRequest, usePageBtn, isStaff))
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
        @PathVariable seminarId: Long
    ): ResponseEntity<SeminarDto> {
        return ResponseEntity.ok(seminarService.readSeminar(seminarId))
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
}
