package com.wafflestudio.csereal.core.research.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.member.database.ProfessorRepository
import com.wafflestudio.csereal.core.research.database.LabEntity
import com.wafflestudio.csereal.core.research.database.LabRepository
import com.wafflestudio.csereal.core.research.database.ResearchEntity
import com.wafflestudio.csereal.core.research.database.ResearchRepository
import com.wafflestudio.csereal.core.research.dto.LabDto
import com.wafflestudio.csereal.core.research.dto.ResearchDto
import com.wafflestudio.csereal.core.research.dto.ResearchGroupResponse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface ResearchService {
    fun createResearchDetail(request: ResearchDto): ResearchDto
    fun readAllResearchGroups(): ResearchGroupResponse
    fun readAllResearchCenters(): List<ResearchDto>
    fun updateResearchDetail(researchId: Long, request: ResearchDto): ResearchDto
    fun createLab(request: LabDto): LabDto

    fun readAllLabs(): List<LabDto>
}

@Service
class ResearchServiceImpl(
    private val researchRepository: ResearchRepository,
    private val labRepository: LabRepository,
    private val professorRepository: ProfessorRepository
) : ResearchService {
    @Transactional
    override fun createResearchDetail(request: ResearchDto): ResearchDto {
        val newResearch = ResearchEntity.of(request)
        if(request.labsId != null) {

            for(labId in request.labsId) {
                val lab = labRepository.findByIdOrNull(labId)
                    ?: throw CserealException.Csereal404("해당 연구실을 찾을 수 없습니다.(labId=$labId)")
                newResearch.labs.add(lab)
                lab.research = newResearch
            }
        }

        researchRepository.save(newResearch)

        return ResearchDto.of(newResearch)
    }

    @Transactional
    override fun readAllResearchGroups(): ResearchGroupResponse {
        // Todo: description 수정 필요
        val description = "세계가 주목하는 컴퓨터공학부의 많은 교수들은 ACM, IEEE 등 " +
                "세계적인 컴퓨터관련 주요 학회에서 국제학술지 편집위원, 국제학술회의 위원장, 기조연설자 등으로 활발하게 활동하고 있습니다. " +
                "정부 지원과제, 민간 산업체 지원 연구과제 등도 성공적으로 수행, 우수한 성과들을 내놓고 있으며, " +
                "오늘도 인류가 꿈꾸는 행복하고 편리한 세상을 위해 변화와 혁신, 연구와 도전을 계속하고 있습니다."

        val researchDetails = researchRepository.findAllByPostTypeOrderByName("groups").map {
            ResearchDto.of(it)
        }

        return ResearchGroupResponse(description, researchDetails)
    }

    @Transactional(readOnly = true)
    override fun readAllResearchCenters(): List<ResearchDto> {
        val researchDetails = researchRepository.findAllByPostTypeOrderByName("centers").map {
            ResearchDto.of(it)
        }

        return researchDetails
    }
    @Transactional
    override fun updateResearchDetail(researchId: Long, request: ResearchDto): ResearchDto {
        val research = researchRepository.findByIdOrNull(researchId)
            ?: throw CserealException.Csereal404("해당 게시글을 찾을 수 없습니다.(researchId=$researchId)")

        if(request.labsId != null) {
            for(labId in request.labsId) {
                val lab = labRepository.findByIdOrNull(labId)
                    ?: throw CserealException.Csereal404("해당 연구실을 찾을 수 없습니다.(labId=$labId)")

            }

            val oldLabs = research.labs.map { it.id }

            val labsToRemove = oldLabs - request.labsId
            val labsToAdd = request.labsId - oldLabs

            research.labs.removeIf { it.id in labsToRemove}

            for(labsToAddId in labsToAdd) {
                val lab = labRepository.findByIdOrNull(labsToAddId)!!
                research.labs.add(lab)
                lab.research = research

            }
        }

        return ResearchDto.of(research)
    }

    @Transactional
    override fun createLab(request: LabDto): LabDto {
        val researchGroup = researchRepository.findByName(request.group)
            ?: throw CserealException.Csereal404("해당 연구그룹을 찾을 수 없습니다.(researchGroupId = ${request.group}")

        if(researchGroup.postType != "groups") {
            throw CserealException.Csereal404("해당 게시글은 연구그룹이어야 합니다.")
        }

        // get을 우선 구현하기 위해 빼겠습니다
        /*
        if(request.professorsId != null) {
            for(professorId in request.professorsId) {
                val professor = professorRepository.findByIdOrNull(professorId)
                    ?: throw CserealException.Csereal404("해당 교수님을 찾을 수 없습니다.(professorId = $professorId")
            }
        }

         */
        val newLab = LabEntity.of(researchGroup, request)

        labRepository.save(newLab)
        return LabDto.of(newLab)
    }

    @Transactional
    override fun readAllLabs(): List<LabDto> {
        val labs = labRepository.findAllByOrderByName().map {
            LabDto.of(it)
        }

        return labs
    }
}