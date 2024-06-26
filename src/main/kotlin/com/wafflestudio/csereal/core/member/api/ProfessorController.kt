package com.wafflestudio.csereal.core.member.api

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.member.dto.ProfessorDto
import com.wafflestudio.csereal.core.member.dto.ProfessorPageDto
import com.wafflestudio.csereal.core.member.dto.SimpleProfessorDto
import com.wafflestudio.csereal.core.member.service.ProfessorService
import org.springframework.context.annotation.Profile
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
        @RequestPart("request") createProfessorRequest: ProfessorDto,
        @RequestPart("mainImage") mainImage: MultipartFile?
    ): ResponseEntity<ProfessorDto> {
        return ResponseEntity.ok(professorService.createProfessor(createProfessorRequest, mainImage))
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
    @PatchMapping("/{professorId}")
    fun updateProfessor(
        @PathVariable professorId: Long,
        @RequestPart("request") updateProfessorRequest: ProfessorDto,
        @RequestPart("mainImage") mainImage: MultipartFile?
    ): ResponseEntity<ProfessorDto> {
        return ResponseEntity.ok(
            professorService.updateProfessor(professorId, updateProfessorRequest, mainImage)
        )
    }

    @AuthenticatedStaff
    @DeleteMapping("/{professorId}")
    fun deleteProfessor(@PathVariable professorId: Long): ResponseEntity<Any> {
        professorService.deleteProfessor(professorId)
        return ResponseEntity.ok().build()
    }

    @Profile("!prod")
    @PostMapping("/migrate")
    fun migrateProfessors(
        @RequestBody requestList: List<ProfessorDto>
    ): ResponseEntity<List<ProfessorDto>> {
        return ResponseEntity.ok(professorService.migrateProfessors(requestList))
    }

    @Profile("!prod")
    @PatchMapping("/migrateImage/{professorId}")
    fun migrateProfessorImage(
        @PathVariable professorId: Long,
        @RequestPart("mainImage") mainImage: MultipartFile
    ): ResponseEntity<ProfessorDto> {
        return ResponseEntity.ok(professorService.migrateProfessorImage(professorId, mainImage))
    }
}
