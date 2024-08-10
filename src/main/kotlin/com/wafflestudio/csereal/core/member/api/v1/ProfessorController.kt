package com.wafflestudio.csereal.core.member.api.v1

import com.wafflestudio.csereal.core.member.dto.ProfessorDto
import com.wafflestudio.csereal.core.member.dto.ProfessorPageDto
import com.wafflestudio.csereal.core.member.dto.SimpleProfessorDto
import com.wafflestudio.csereal.core.member.service.ProfessorService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Deprecated(message = "Use V2 API")
@RequestMapping("/api/v1/professor")
@RestController("ProfessorControllerV1")
class ProfessorController(
    private val professorService: ProfessorService
) {
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
}
