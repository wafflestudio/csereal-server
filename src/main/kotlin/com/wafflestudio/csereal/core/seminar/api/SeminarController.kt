package com.wafflestudio.csereal.core.seminar.api

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.common.mockauth.CustomPrincipal
import com.wafflestudio.csereal.core.seminar.dto.SeminarDto
import com.wafflestudio.csereal.core.seminar.dto.SeminarSearchResponse
import com.wafflestudio.csereal.core.seminar.service.SeminarService
import com.wafflestudio.csereal.core.user.database.Role
import com.wafflestudio.csereal.core.user.database.UserRepository
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
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
        @RequestParam(required = false, defaultValue = "10") pageSize: Int,
        authentication: Authentication?
    ): ResponseEntity<SeminarSearchResponse> {
        val principal = authentication?.principal

        val isStaff = principal?.let {
            val username = when (principal) {
                is OidcUser -> principal.idToken.getClaim("username")
                is CustomPrincipal -> principal.userEntity.username
                else -> throw CserealException.Csereal401("Unsupported principal type")
            }
            val user = userRepository.findByUsername(username)
            user?.role == Role.ROLE_STAFF
        } ?: false

        val usePageBtn = pageNum != null
        val page = pageNum ?: 1
        val pageRequest = PageRequest.of(page - 1, pageSize)
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
