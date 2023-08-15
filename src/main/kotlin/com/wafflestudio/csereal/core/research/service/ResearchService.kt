package com.wafflestudio.csereal.core.research.service

import com.wafflestudio.csereal.core.research.database.LabEntity
import com.wafflestudio.csereal.core.research.database.ResearchEntity
import com.wafflestudio.csereal.core.research.database.ResearchRepository
import com.wafflestudio.csereal.core.research.dto.ResearchDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface ResearchService {
    fun createResearchDetail(postType: String, name: String, request: ResearchDto): ResearchDto
}

@Service
class ResearchServiceImpl(
    private val researchRepository: ResearchRepository
) : ResearchService {
    @Transactional
    override fun createResearchDetail(postType: String, name: String, request: ResearchDto): ResearchDto {
        val newResearch = ResearchEntity.of(postType, name, request)

        if(request.labs != null) {
            for(lab in request.labs) {
                LabEntity.create(lab, newResearch)
            }
        }

        researchRepository.save(newResearch)

        return ResearchDto.of(newResearch)
    }
}