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

    fun createProfessor(createProfessorRequest: ProfessorDto): ProfessorDto
    fun getProfessor(professorId: Long): ProfessorDto
    fun getActiveProfessors(): List<SimpleProfessorDto>
    fun getInactiveProfessors(): List<SimpleProfessorDto>
    fun updateProfessor(professorId: Long, updateProfessorRequest: ProfessorDto): ProfessorDto
    fun deleteProfessor(professorId: Long)
}

@Service
@Transactional
class ProfessorServiceImpl(
    private val labRepository: LabRepository,
    private val professorRepository: ProfessorRepository
) : ProfessorService {

    override fun createProfessor(createProfessorRequest: ProfessorDto): ProfessorDto {
        val professor = ProfessorEntity.of(createProfessorRequest)

        if (createProfessorRequest.labId != null) {
            val lab = labRepository.findByIdOrNull(createProfessorRequest.labId)
                ?: throw CserealException.Csereal404("해당 연구실을 찾을 수 없습니다. LabId: ${createProfessorRequest.labId}")
            professor.addLab(lab)
        }

        for (education in createProfessorRequest.educations) {
            EducationEntity.create(education, professor)
        }

        for (researchArea in createProfessorRequest.researchAreas) {
            ResearchAreaEntity.create(researchArea, professor)
        }

        for (career in createProfessorRequest.careers) {
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
        return professorRepository.findByIsActiveTrue().map { SimpleProfessorDto.of(it) }.sortedBy { it.name }
    }

    @Transactional(readOnly = true)
    override fun getInactiveProfessors(): List<SimpleProfessorDto> {
        return professorRepository.findByIsActiveFalse().map { SimpleProfessorDto.of(it) }.sortedBy { it.name }
    }

    override fun updateProfessor(professorId: Long, updateProfessorRequest: ProfessorDto): ProfessorDto {

        val professor = professorRepository.findByIdOrNull(professorId)
            ?: throw CserealException.Csereal404("해당 교수님을 찾을 수 없습니다. professorId: ${professorId}")

        if (updateProfessorRequest.labId != null && updateProfessorRequest.labId != professor.lab?.id) {
            val lab = labRepository.findByIdOrNull(updateProfessorRequest.labId)
                ?: throw CserealException.Csereal404("해당 연구실을 찾을 수 없습니다. LabId: ${updateProfessorRequest.labId}")
            professor.addLab(lab)
        }

        professor.update(updateProfessorRequest)

        // 학력 업데이트
        val oldEducations = professor.educations.map { it.name }

        val educationsToRemove = oldEducations - updateProfessorRequest.educations
        val educationsToAdd = updateProfessorRequest.educations - oldEducations

        professor.educations.removeIf { it.name in educationsToRemove }

        for (education in educationsToAdd) {
            EducationEntity.create(education, professor)
        }

        // 연구 분야 업데이트
        val oldResearchAreas = professor.researchAreas.map { it.name }

        val researchAreasToRemove = oldResearchAreas - updateProfessorRequest.researchAreas
        val researchAreasToAdd = updateProfessorRequest.researchAreas - oldResearchAreas

        professor.researchAreas.removeIf { it.name in researchAreasToRemove }

        for (researchArea in researchAreasToAdd) {
            ResearchAreaEntity.create(researchArea, professor)
        }

        // 경력 업데이트
        val oldCareers = professor.careers.map { it.name }

        val careersToRemove = oldCareers - updateProfessorRequest.careers
        val careersToAdd = updateProfessorRequest.careers - oldCareers

        professor.careers.removeIf { it.name in careersToRemove }

        for (career in careersToAdd) {
            CareerEntity.create(career, professor)
        }

        return ProfessorDto.of(professor)
    }

    override fun deleteProfessor(professorId: Long) {
        professorRepository.deleteById(professorId)
    }

}
