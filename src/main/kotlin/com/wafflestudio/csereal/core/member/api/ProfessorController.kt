package com.wafflestudio.csereal.core.member.api

import com.wafflestudio.csereal.core.member.dto.ProfessorDto
import com.wafflestudio.csereal.core.member.dto.ProfessorPageDto
import com.wafflestudio.csereal.core.member.dto.SimpleProfessorDto
import com.wafflestudio.csereal.core.member.service.ProfessorService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/professor")
@RestController
class ProfessorController(
    private val professorService: ProfessorService
) {

    @PostMapping
    fun createProfessor(
        @RequestPart("request") createProfessorRequest: ProfessorDto,
        @RequestPart("mainImage") mainImage: MultipartFile?,
    ): ResponseEntity<ProfessorDto> {
        return ResponseEntity.ok(professorService.createProfessor(createProfessorRequest, mainImage))
    }

    @GetMapping("/{professorId}")
    fun getProfessor(@PathVariable professorId: Long): ResponseEntity<ProfessorDto> {
        return ResponseEntity.ok(professorService.getProfessor(professorId))
    }

    @GetMapping("/active")
    fun getActiveProfessors(): ResponseEntity<ProfessorPageDto> {
        return ResponseEntity.ok(professorService.getActiveProfessors())
    }

    @GetMapping("/inactive")
    fun getInactiveProfessors(): ResponseEntity<List<SimpleProfessorDto>> {
        return ResponseEntity.ok(professorService.getInactiveProfessors())
    }

    @PatchMapping("/{professorId}")
    fun updateProfessor(
        @PathVariable professorId: Long,
        @RequestBody updateProfessorRequest: ProfessorDto
    ): ResponseEntity<ProfessorDto> {
        return ResponseEntity.ok(professorService.updateProfessor(professorId, updateProfessorRequest))
    }

    @DeleteMapping("/{professorId}")
    fun deleteProfessor(@PathVariable professorId: Long): ResponseEntity<Any> {
        professorService.deleteProfessor(professorId)
        return ResponseEntity.ok().build()
    }

}
