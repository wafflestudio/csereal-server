package com.wafflestudio.csereal.core.member.api.v3

import com.wafflestudio.csereal.core.member.api.req.CreateProfessorLanguagesReqBody
import com.wafflestudio.csereal.core.member.api.req.ModifyProfessorLanguagesReqBody
import com.wafflestudio.csereal.core.member.dto.ProfessorLanguagesDto
import com.wafflestudio.csereal.core.member.dto.ProfessorPageDto
import com.wafflestudio.csereal.core.member.dto.SimpleProfessorDto
import com.wafflestudio.csereal.core.member.service.ProfessorService
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * v3 교수 API — 이중언어 콘텐츠를 단일 리소스로.
 *
 * v2 대비: 수정/삭제가 dual-id(`/{koProfessorId}/{enProfessorId}`)가 아니라 단일 id(쌍은
 * member_language에서 서버가 해소). 그리고 **파일 파트명을 create/update 모두 `mainImage`로 통일**
 * (v2는 create=`mainImage`, update=`newMainImage`로 갈라져 프론트가 분기해야 했다).
 */
@RequestMapping("/api/v3/professor")
@RestController
class ProfessorV3Controller(
    private val professorService: ProfessorService
) {
    @GetMapping("/{professorId}")
    fun getProfessor(
        @PathVariable @Positive
        professorId: Long
    ): ProfessorLanguagesDto = professorService.getProfessorLanguages(professorId)

    @GetMapping("/active")
    fun getActiveProfessors(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ProfessorPageDto = professorService.getActiveProfessors(language)

    @GetMapping("/inactive")
    fun getInactiveProfessors(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): List<SimpleProfessorDto> = professorService.getInactiveProfessors(language)

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping(consumes = ["multipart/form-data"])
    fun createProfessor(
        @RequestPart("request") request: CreateProfessorLanguagesReqBody,
        @RequestPart("mainImage") mainImage: MultipartFile?
    ): ProfessorLanguagesDto = professorService.createProfessorLanguages(request, mainImage)

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{professorId}", consumes = ["multipart/form-data"])
    fun updateProfessor(
        @PathVariable @Positive
        professorId: Long,
        @RequestPart("request") request: ModifyProfessorLanguagesReqBody,
        @RequestPart("mainImage") mainImage: MultipartFile?
    ): ProfessorLanguagesDto = professorService.updateProfessorById(professorId, request, mainImage)

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/{professorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProfessor(
        @PathVariable @Positive
        professorId: Long
    ) = professorService.deleteProfessorById(professorId)
}
