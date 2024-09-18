package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.core.member.database.MemberSearchEntity
import com.wafflestudio.csereal.core.member.database.ProfessorEntity
import com.wafflestudio.csereal.core.member.database.ProfessorRepository
import com.wafflestudio.csereal.core.research.database.LabRepository
import com.wafflestudio.csereal.core.research.event.LabCreatedEvent
import com.wafflestudio.csereal.core.research.event.LabDeletedEvent
import com.wafflestudio.csereal.core.research.event.LabModifiedEvent
import org.springframework.context.event.EventListener
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface ProfessorEventService {
    fun labDeletedEventListener(event: LabDeletedEvent)
    fun labModifiedEventListener(event: LabModifiedEvent)
    fun labCreatedEventListener(event: LabCreatedEvent)
}

@Service
class ProfessorEventServiceImpl(
    private val professorRepository: ProfessorRepository,
    private val labRepository: LabRepository,
) : ProfessorEventService {
    @EventListener
    @Transactional
    override fun labCreatedEventListener(event: LabCreatedEvent) {
        if (event.professorIds.isEmpty()) {
            return
        }

        val lab = labRepository.findByIdOrNull(event.id)!!
        val professors = professorRepository.findAllById(event.professorIds)
            .takeIf { it.size == event.professorIds.size }!!

        // TODO: Consider professor's before lab value
        professors.forEach {
            it.lab = lab
            upsertProfessorSearchIndex(it)
        }
    }

    @EventListener
    @Transactional
    override fun labModifiedEventListener(event: LabModifiedEvent) {
        val lab = labRepository.findByIdOrNull(event.id)!!

        val oldProfessorIds = event.professorIdsModified.first
        val oldProfessors = oldProfessorIds.let { p ->
            professorRepository.findAllById(p)
                .takeIf { it.size == p.size }!!
        }

        val newProfessorIds = event.professorIdsModified.second
        val newProfessors = newProfessorIds.let { p ->
            professorRepository.findAllById(p)
                .takeIf { it.size == p.size }!!
        }

        when {
            oldProfessors.isEmpty() && newProfessors.isEmpty() -> {}

            oldProfessors.isEmpty() && newProfessors.isNotEmpty() -> {
                newProfessors.forEach {
                    it.lab = lab
                    upsertProfessorSearchIndex(it)
                }
            }

            oldProfessors.isNotEmpty() && newProfessors.isEmpty() -> {
                oldProfessors.forEach {
                    it.lab = null
                    upsertProfessorSearchIndex(it)
                }
            }

            oldProfessorIds == newProfessorIds -> {
                oldProfessors.forEach { upsertProfessorSearchIndex(it) }
            }

            else -> {
                val removeProfessorIds = oldProfessorIds - newProfessorIds
                oldProfessors.forEach {
                    if (it.id in removeProfessorIds) {
                        it.lab = null
                    }
                    upsertProfessorSearchIndex(it)
                }

                val addProfessorIds = newProfessorIds - oldProfessorIds
                newProfessors.forEach {
                    if (it.id in addProfessorIds) {
                        it.lab = lab
                    }
                    upsertProfessorSearchIndex(it)
                }
            }
        }
    }

    @EventListener
    @Transactional
    override fun labDeletedEventListener(event: LabDeletedEvent) {
        val lab = labRepository.findByIdOrNull(event.id)!!
        val professors = professorRepository.findAllById(event.professorIds)
            .takeIf { it.size == event.professorIds.size }!!

        professors.forEach {
            it.lab = null
            upsertProfessorSearchIndex(it)
        }
    }

    @Transactional
    fun upsertProfessorSearchIndex(professor: ProfessorEntity) {
        professor.memberSearch?.update(professor)
            ?: let { professor.memberSearch = MemberSearchEntity.create(professor) }
    }
}
