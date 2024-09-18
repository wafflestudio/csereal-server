package com.wafflestudio.csereal.core.research.service

import com.wafflestudio.csereal.core.member.event.ProfessorCreatedEvent
import com.wafflestudio.csereal.core.member.event.ProfessorDeletedEvent
import com.wafflestudio.csereal.core.member.event.ProfessorModifiedEvent
import com.wafflestudio.csereal.core.research.database.LabEntity
import com.wafflestudio.csereal.core.research.database.LabRepository
import com.wafflestudio.csereal.core.research.database.ResearchSearchEntity
import org.springframework.context.event.EventListener
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface LabEventService {
    fun professorCreatedEventListener(professorCreatedEvent: ProfessorCreatedEvent)
    fun professorDeletedEventListener(professorDeletedEvent: ProfessorDeletedEvent)
    fun professorModifiedEventListener(professorModifiedEvent: ProfessorModifiedEvent)
}

@Service
class LabEventServiceImpl(
    private val labRepository: LabRepository,
) : LabEventService {
    @EventListener
    @Transactional
    override fun professorCreatedEventListener(professorCreatedEvent: ProfessorCreatedEvent) {
        val lab = professorCreatedEvent.labId?.let {
            labRepository.findByIdOrNull(it)
        } ?: return

        upsertLabSearchIndex(lab)
    }

    @EventListener
    @Transactional
    override fun professorDeletedEventListener(professorDeletedEvent: ProfessorDeletedEvent) {
        val lab = professorDeletedEvent.labId?.let {
            labRepository.findByIdOrNull(it)
        } ?: return

        // if lab still has professor, remove it
        lab.professors.removeIf { it.id == professorDeletedEvent.id }

        // update search data
        upsertLabSearchIndex(lab)
    }

    @EventListener
    @Transactional
    override fun professorModifiedEventListener(professorModifiedEvent: ProfessorModifiedEvent) {
        val beforeLab = professorModifiedEvent.beforeLabId?.let {
            labRepository.findByIdOrNull(it)
        }

        val afterLab = professorModifiedEvent.afterLabId?.let {
            labRepository.findByIdOrNull(it)
        }

        if (beforeLab != null && beforeLab == afterLab) {
            beforeLab.researchSearch?.update(beforeLab)
        }

        beforeLab?.apply {
            // if lab still has professor, remove it
            professors.removeIf { it.id == professorModifiedEvent.id }
        }?.let {
            upsertLabSearchIndex(it)
        }

        afterLab?.let {
            upsertLabSearchIndex(it)
        }
    }

    @Transactional
    fun upsertLabSearchIndex(lab: LabEntity) {
        lab.researchSearch?.update(lab) ?: let {
            lab.researchSearch = ResearchSearchEntity.create(lab)
        }
    }
}
