package com.wafflestudio.csereal.core.member.api

import com.wafflestudio.csereal.core.member.dto.ProfessorDto
import com.wafflestudio.csereal.core.member.dto.SimpleProfessorDto
import com.wafflestudio.csereal.core.member.service.ProfessorService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/professor")
@RestController
class ProfessorController(
    private val professorService: ProfessorService
) {

    @PostMapping
    fun createProfessor(@RequestBody createProfessorRequest: ProfessorDto): ResponseEntity<ProfessorDto> {
        return ResponseEntity.ok(professorService.createProfessor(createProfessorRequest))
    }

    @GetMapping("/{professorId}")
    fun getProfessor(@PathVariable professorId: Long): ResponseEntity<ProfessorDto> {
        return ResponseEntity.ok(professorService.getProfessor(professorId))
    }

    @GetMapping("/active")
    fun getActiveProfessors(): ResponseEntity<List<SimpleProfessorDto>> {
        return ResponseEntity.ok(professorService.getActiveProfessors())
    }

    @GetMapping("/inactive")
    fun getInactiveProfessors(): ResponseEntity<List<SimpleProfessorDto>> {
        return ResponseEntity.ok(professorService.getInactiveProfessors())
    }

    @PatchMapping
    fun updateProfessor(@RequestBody updateProfessorRequest: ProfessorDto): ResponseEntity<ProfessorDto> {
        return ResponseEntity.ok(professorService.updateProfessor(updateProfessorRequest))
    }

    @DeleteMapping("/{professorId}")
    fun deleteProfessor(@PathVariable professorId: Long): ResponseEntity<Any> {
        professorService.deleteProfessor(professorId)
        return ResponseEntity.ok().build()
    }

}
