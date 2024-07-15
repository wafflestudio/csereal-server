package com.wafflestudio.csereal.core.member.api

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.member.api.req.CreateProfessorReqBody
import com.wafflestudio.csereal.core.member.api.req.ModifyProfessorReqBody
import com.wafflestudio.csereal.core.member.dto.ProfessorDto
import com.wafflestudio.csereal.core.member.dto.ProfessorPageDto
import com.wafflestudio.csereal.core.member.dto.SimpleProfessorDto
import com.wafflestudio.csereal.core.member.service.ProfessorService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v1/professor")
@RestController
class ProfessorController(
    private val professorService: ProfessorService
) {

    @AuthenticatedStaff
    @PostMapping
    fun createProfessor(
        @RequestPart("request") createProfessorRequest: CreateProfessorReqBody,
        @RequestPart("mainImage") mainImage: MultipartFile?
    ): ProfessorDto {
        return professorService.createProfessor(createProfessorRequest, mainImage)
    }

    @GetMapping("/{professorId}")
    fun getProfessor(@PathVariable professorId: Long): ResponseEntity<ProfessorDto> {
        return ResponseEntity.ok(professorService.getProfessor(professorId))
    }

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
    @PutMapping("/{professorId}")
    fun updateProfessor(
        @PathVariable professorId: Long,
        @RequestPart("request") updateProfessorRequest: ModifyProfessorReqBody,

        @Parameter(description = "image 교체할 경우 업로드. Request Body의 removeImage 관계없이 변경됨.")
        @RequestPart("newImage")
        newImage: MultipartFile?
    ): ResponseEntity<ProfessorDto> {
        return ResponseEntity.ok(
            professorService.updateProfessor(professorId, updateProfessorRequest, newImage)
        )
    }

    @AuthenticatedStaff
    @DeleteMapping("/{professorId}")
    fun deleteProfessor(@PathVariable professorId: Long): ResponseEntity<Any> {
        professorService.deleteProfessor(professorId)
        return ResponseEntity.ok().build()
    }
}
