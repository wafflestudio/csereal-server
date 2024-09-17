package com.wafflestudio.csereal.core.research.service

import com.wafflestudio.csereal.core.member.event.ProfessorCreatedEvent
import com.wafflestudio.csereal.core.member.event.ProfessorDeletedEvent
import com.wafflestudio.csereal.core.member.event.ProfessorModifiedEvent
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

        lab.researchSearch?.update(lab) ?: let {
            lab.researchSearch = ResearchSearchEntity.create(lab)
        }
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
        lab.researchSearch?.update(lab)
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

        beforeLab?.run {
            // if lab still has professor, remove it
            professors.removeIf { it.id == professorModifiedEvent.id }
            researchSearch?.update(this)
        }

        afterLab?.run {
            researchSearch?.update(this)
        }
    }


}
