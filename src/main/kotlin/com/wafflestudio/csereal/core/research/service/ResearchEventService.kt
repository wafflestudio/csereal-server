package com.wafflestudio.csereal.core.research.service

import com.wafflestudio.csereal.core.research.database.LabRepository
import com.wafflestudio.csereal.core.research.database.ResearchEntity
import com.wafflestudio.csereal.core.research.database.ResearchRepository
import com.wafflestudio.csereal.core.research.database.ResearchSearchEntity
import com.wafflestudio.csereal.core.research.event.LabCreatedEvent
import com.wafflestudio.csereal.core.research.event.LabDeletedEvent
import com.wafflestudio.csereal.core.research.event.LabModifiedEvent
import org.springframework.context.event.EventListener
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface ResearchEventService {
    fun labDeletedEventListener(event: LabDeletedEvent)
    fun labModifiedEventListener(event: LabModifiedEvent)
    fun labCreatedEventListener(event: LabCreatedEvent)
}

@Service
class ResearchEventServiceImpl(
    private val researchRepository: ResearchRepository,
    private val labRepository: LabRepository,
) : ResearchEventService {
    @EventListener
    @Transactional
    override fun labCreatedEventListener(event: LabCreatedEvent) {
        if (event.researchId == null) {
            return
        }

        val lab = labRepository.findByIdOrNull(event.id)!!
        val research = researchRepository.findByIdOrNull(event.researchId)!! // should exist
        research.labs.add(lab)

        upsertResearchSearchIndex(research)
    }

    @EventListener
    @Transactional
    override fun labModifiedEventListener(event: LabModifiedEvent) {
        val lab = labRepository.findByIdOrNull(event.id)!!

        val oldResearch = event.researchIdModified.first?.let { researchRepository.findByIdOrNull(it)!! }
        val newResearch = event.researchIdModified.second?.let { researchRepository.findByIdOrNull(it)!! }

        when {
            oldResearch == null && newResearch == null -> {}
            oldResearch == null && newResearch != null -> {
                newResearch.apply {
                    labs.add(lab)
                }.let {
                    upsertResearchSearchIndex(it)
                }
            }

            oldResearch != null && newResearch == null -> {
                oldResearch.apply {
                    labs.remove(lab)
                }.let {
                    upsertResearchSearchIndex(it)
                }
            }

            oldResearch!!.id == newResearch!!.id -> {
                upsertResearchSearchIndex(oldResearch)
            }

            else -> {
                oldResearch.apply {
                    labs.remove(lab)
                }.let {
                    upsertResearchSearchIndex(it)
                }
                newResearch.apply {
                    labs.add(lab)
                }.let {
                    upsertResearchSearchIndex(it)
                }
            }
        }
    }

    @EventListener
    @Transactional
    override fun labDeletedEventListener(event: LabDeletedEvent) {
        if (event.researchId == null) {
            return
        }

        val lab = labRepository.findByIdOrNull(event.id)!!
        val research = researchRepository.findByIdOrNull(event.researchId)!!
        research.labs.remove(lab)

        upsertResearchSearchIndex(research)
    }

    @Transactional
    fun upsertResearchSearchIndex(research: ResearchEntity) {
        research.researchSearch?.update(research) ?: let {
            research.researchSearch = ResearchSearchEntity.create(research)
        }
    }
}
