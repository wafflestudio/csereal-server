package com.wafflestudio.csereal.core.member.api.v2

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.member.api.req.CreateProfessorLanguagesReqBody
import com.wafflestudio.csereal.core.member.api.req.ModifyProfessorLanguagesReqBody
import com.wafflestudio.csereal.core.member.dto.ProfessorLanguagesDto
import com.wafflestudio.csereal.core.member.dto.ProfessorPageDto
import com.wafflestudio.csereal.core.member.dto.SimpleProfessorDto
import com.wafflestudio.csereal.core.member.service.ProfessorService
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v2/professor")
@RestController
class ProfessorController(
    private val professorService: ProfessorService
) {
    @GetMapping("/{professorId}")
    fun getProfessor(
        @PathVariable @Positive
        professorId: Long
    ): ProfessorLanguagesDto =
        professorService.getProfessorLanguages(professorId)

    @GetMapping("/active")
    fun getActiveProfessors(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<ProfessorPageDto> {
        return ResponseEntity.ok(professorService.getActiveProfessors(language))
    }

    @GetMapping("/inactive")
    fun getInactiveProfessors(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<List<SimpleProfessorDto>> {
        return ResponseEntity.ok(professorService.getInactiveProfessors(language))
    }

    @AuthenticatedStaff
    @PostMapping(consumes = ["multipart/form-data"])
    fun createProfessor(
        @RequestPart("request") requestBody: CreateProfessorLanguagesReqBody,
        @RequestPart("image") image: MultipartFile?
    ): ProfessorLanguagesDto =
        professorService.createProfessorLanguages(requestBody, image)

    @AuthenticatedStaff
    @PutMapping("/{koProfessorId}/{enProfessorId}", consumes = ["multipart/form-data"])
    fun updateProfessor(
        @PathVariable @Positive
        koProfessorId: Long,
        @PathVariable @Positive
        enProfessorId: Long,
        @RequestPart("request") requestBody: ModifyProfessorLanguagesReqBody,

        @Parameter(description = "image 교체할 경우 업로드. Request Body의 removeImage 관계없이 변경됨.")
        @RequestPart("newImage")
        newImage: MultipartFile?
    ): ProfessorLanguagesDto =
        professorService.updateProfessorLanguages(koProfessorId, enProfessorId, requestBody, newImage)

    @AuthenticatedStaff
    @DeleteMapping("/{koProfessorId}/{enProfessorId}")
    fun deleteProfessor(
        @PathVariable @Positive
        koProfessorId: Long,
        @PathVariable @Positive
        enProfessorId: Long
    ) = professorService.deleteProfessorLanguages(koProfessorId, enProfessorId)
}
