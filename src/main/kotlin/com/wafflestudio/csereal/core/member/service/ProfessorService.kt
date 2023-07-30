package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.member.database.*
import com.wafflestudio.csereal.core.member.dto.ProfessorDto
import com.wafflestudio.csereal.core.member.dto.SimpleProfessorDto
import com.wafflestudio.csereal.core.research.database.LabRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface ProfessorService {
    fun createProfessor(professorDto: ProfessorDto): ProfessorDto
    fun getProfessor(professorId: Long): ProfessorDto
    fun getActiveProfessors(): List<SimpleProfessorDto>
    fun getInactiveProfessors(): List<SimpleProfessorDto>
    fun updateProfessor(updateProfessorRequest: ProfessorDto): ProfessorDto
    fun deleteProfessor(professorId: Long)
}

@Service
@Transactional
class ProfessorServiceImpl(
    private val labRepository: LabRepository,
    private val professorRepository: ProfessorRepository
) : ProfessorService {

    override fun createProfessor(professorDto: ProfessorDto): ProfessorDto {
        val professor = ProfessorEntity.of(professorDto)

        if (professorDto.labId != null) {
            val lab = labRepository.findByIdOrNull(professorDto.labId)
                ?: throw CserealException.Csereal404("해당 연구실을 찾을 수 없습니다. LabId: ${professorDto.labId}")
            professor.addLab(lab)
        }

        for (education in professorDto.educations) {
            EducationEntity.create(education, professor)
        }

        for (researchArea in professorDto.researchAreas) {
            ResearchAreaEntity.create(researchArea, professor)
        }

        for (career in professorDto.careers) {
            CareerEntity.create(career, professor)
        }

        professorRepository.save(professor)

        return ProfessorDto.of(professor)
    }

    @Transactional(readOnly = true)
    override fun getProfessor(professorId: Long): ProfessorDto {
        val professor = professorRepository.findByIdOrNull(professorId)
            ?: throw CserealException.Csereal404("해당 교수님을 찾을 수 없습니다. professorId: ${professorId}")
        return ProfessorDto.of(professor)
    }

    @Transactional(readOnly = true)
    override fun getActiveProfessors(): List<SimpleProfessorDto> {
        return professorRepository.findByIsActiveTrue().map { SimpleProfessorDto.of(it) }
    }

    @Transactional(readOnly = true)
    override fun getInactiveProfessors(): List<SimpleProfessorDto> {
        return professorRepository.findByIsActiveFalse().map { SimpleProfessorDto.of(it) }
    }

    override fun updateProfessor(updateProfessorRequest: ProfessorDto): ProfessorDto {
        TODO("Not yet implemented")
    }

    override fun deleteProfessor(professorId: Long) {
        TODO("Not yet implemented")
    }

}
