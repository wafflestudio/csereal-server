package com.wafflestudio.csereal.core.research.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.research.database.LabEntity
import com.wafflestudio.csereal.core.research.database.LabRepository
import com.wafflestudio.csereal.core.research.database.ResearchEntity
import com.wafflestudio.csereal.core.research.database.ResearchRepository
import com.wafflestudio.csereal.core.research.dto.LabDto
import com.wafflestudio.csereal.core.research.dto.ResearchDto
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface ResearchService {
    fun createResearchDetail(request: ResearchDto): ResearchDto
    fun createLab(request: LabDto): LabDto
}

@Service
class ResearchServiceImpl(
    private val researchRepository: ResearchRepository,
    private val labRepository: LabRepository,
) : ResearchService {
    @Transactional
    override fun createResearchDetail(request: ResearchDto): ResearchDto {
        val newResearch = ResearchEntity.of(request)
        val list : MutableList<LabEntity> = mutableListOf()
        if(request.labsId != null) {

            for(labId in request.labsId) {
                val lab = labRepository.findByIdOrNull(labId)
                    ?: throw CserealException.Csereal404("해당 연구실을 찾을 수 없습니다.(labId=$labId)")
                list.add(lab)
            }
        }

            newResearch.labs = list
        researchRepository.save(newResearch)

        return ResearchDto.of(newResearch)
    }

    @Transactional
    override fun createLab(request: LabDto): LabDto {
        val researchGroup = researchRepository.findByIdOrNull(request.researchGroupId)
            ?: throw CserealException.Csereal404("해당 연구그룹을 찾을 수 없습니다.(researchGroupId = ${request.researchGroupId}")
        val newLab = LabEntity.of(researchGroup, request)

        labRepository.save(newLab)
        return LabDto.of(researchGroup.id, newLab)
    }
}