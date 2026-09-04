package com.wafflestudio.csereal.core.research.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.database.ProfessorRepository
import com.wafflestudio.csereal.core.research.api.req.CreateLabLanguageReqBody
import com.wafflestudio.csereal.core.research.api.req.ModifyLabLanguageReqBody
import com.wafflestudio.csereal.core.research.database.LabEntity
import com.wafflestudio.csereal.core.research.database.LabRepository
import com.wafflestudio.csereal.core.research.database.LabTranslationEntity
import com.wafflestudio.csereal.core.research.database.LabTranslationRepository
import com.wafflestudio.csereal.core.research.database.ResearchRepository
import com.wafflestudio.csereal.core.research.database.ResearchSearchEntity
import com.wafflestudio.csereal.core.research.dto.LabDto
import com.wafflestudio.csereal.core.research.dto.LabLanguageDto
import com.wafflestudio.csereal.core.research.event.LabCreatedEvent
import com.wafflestudio.csereal.core.research.event.LabDeletedEvent
import com.wafflestudio.csereal.core.research.event.LabModifiedEvent
import com.wafflestudio.csereal.core.research.type.ResearchType
import com.wafflestudio.csereal.core.resource.attachment.service.AttachmentService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface LabService {
    fun readLabLanguage(labId: Long): LabLanguageDto
    fun readAllLabs(language: LanguageType): List<LabDto>
    fun createLabLanguage(request: CreateLabLanguageReqBody, pdf: MultipartFile?): LabLanguageDto
    fun updateLabLanguage(labId: Long, request: ModifyLabLanguageReqBody, pdf: MultipartFile?): LabLanguageDto
    fun deleteLabLanguage(labId: Long)
}

@Service
@Transactional
class LabServiceImpl(
    private val attachmentService: AttachmentService,
    private val labRepository: LabRepository,
    private val labTranslationRepository: LabTranslationRepository,
    private val researchRepository: ResearchRepository,
    private val professorRepository: ProfessorRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) : LabService {

    @Transactional(readOnly = true)
    override fun readAllLabs(language: LanguageType): List<LabDto> =
        labTranslationRepository.findAllByLanguageOrderByName(language)
            .map { LabDto.of(it, attachmentService.createOneAttachmentResponse(it.lab.pdf)) }

    @Transactional(readOnly = true)
    override fun readLabLanguage(labId: Long): LabLanguageDto {
        val lab = labRepository.findByIdOrNull(labId)
            ?: throw CserealException(ErrorCode.LAB_NOT_FOUND, mapOf("labId" to labId))
        return lab.toLanguageDto()
    }

    @Transactional
    override fun createLabLanguage(request: CreateLabLanguageReqBody, pdf: MultipartFile?): LabLanguageDto {
        val lab = LabEntity(
            acronym = request.acronym,
            tel = request.tel,
            youtube = request.youtube,
            websiteURL = request.websiteURL
        )
        lab.research = request.groupId?.let { resolveGroup(it) }

        listOf(LanguageType.KO to request.ko, LanguageType.EN to request.en).forEach { (language, content) ->
            lab.translations.add(
                LabTranslationEntity(
                    lab = lab,
                    language = language,
                    name = content.name,
                    description = content.description,
                    location = content.location
                )
            )
        }

        val professors = professorRepository.findAllById(request.professorIds)
        if (professors.size != request.professorIds.size) {
            throw CserealException(ErrorCode.PROFESSOR_NOT_FOUND, mapOf("professorIds" to request.professorIds))
        }
        professors.forEach { it.addLab(lab) }

        labRepository.save(lab)

        // PDF 는 연구실에 하나뿐이다 — 예전엔 언어별로 한 번씩 올라가 같은 파일이 두 벌 남았다.
        pdf?.let { attachmentService.uploadAttachmentInLabEntity(lab, it) }
        lab.translations.forEach { it.researchSearch = ResearchSearchEntity.create(it) }

        applicationEventPublisher.publishEvent(
            LabCreatedEvent(lab.id, lab.research?.id, request.professorIds)
        )
        return lab.toLanguageDto()
    }

    @Transactional
    override fun updateLabLanguage(
        labId: Long,
        request: ModifyLabLanguageReqBody,
        pdf: MultipartFile?
    ): LabLanguageDto {
        val lab = labRepository.findByIdOrNull(labId)
            ?: throw CserealException(ErrorCode.LAB_NOT_FOUND, mapOf("labId" to labId))

        val oldGroupId = lab.research?.id
        val oldProfessorIds = lab.professors.map { it.id }.toSet()

        lab.research = request.groupId?.let { resolveGroup(it) }
        lab.acronym = request.acronym
        lab.tel = request.tel
        lab.youtube = request.youtube
        lab.websiteURL = request.websiteURL

        val newProfessors = professorRepository.findAllById(request.professorIds)
        if (newProfessors.size != request.professorIds.size) {
            throw CserealException(ErrorCode.PROFESSOR_NOT_FOUND, mapOf("professorIds" to request.professorIds))
        }
        lab.professors.filterNot { it.id in request.professorIds }.forEach { it.lab = null }
        lab.professors = newProfessors.toMutableSet()
        newProfessors.forEach { it.lab = lab }

        listOf(LanguageType.KO to request.ko, LanguageType.EN to request.en).forEach { (language, content) ->
            val translation = lab.translationOf(language)
                ?: throw CserealException(ErrorCode.LAB_NOT_FOUND, mapOf("labId" to labId))
            translation.name = content.name
            translation.description = content.description
            translation.location = content.location
        }

        if (request.removePdf && pdf == null) {
            lab.pdf?.let {
                lab.pdf = null
                attachmentService.deleteAttachment(it)
            }
        } else if (pdf != null) {
            lab.pdf?.let {
                lab.pdf = null
                attachmentService.deleteAttachment(it)
            }
            attachmentService.uploadAttachmentInLabEntity(lab, pdf)
        }

        lab.translations.forEach { upsertSearchIndex(it) }

        applicationEventPublisher.publishEvent(
            LabModifiedEvent(
                lab.id,
                oldGroupId to lab.research?.id,
                oldProfessorIds to request.professorIds
            )
        )
        return lab.toLanguageDto()
    }

    @Transactional
    override fun deleteLabLanguage(labId: Long) {
        val lab = labRepository.findByIdOrNull(labId)
            ?: throw CserealException(ErrorCode.LAB_NOT_FOUND, mapOf("labId" to labId))

        applicationEventPublisher.publishEvent(
            LabDeletedEvent(lab.id, lab.research?.id, lab.professors.map { it.id }.toSet())
        )

        lab.pdf?.let {
            lab.pdf = null
            attachmentService.deleteAttachment(it)
        }
        // 번역본과 검색 색인은 cascade + orphanRemoval 로 함께 지워진다.
        labRepository.delete(lab)
    }

    private fun resolveGroup(groupId: Long) =
        researchRepository.findByIdOrNull(groupId)
            ?.also {
                if (it.postType != ResearchType.GROUPS) {
                    throw CserealException(ErrorCode.NOT_A_RESEARCH_GROUP, mapOf("id" to it.id))
                }
            }
            ?: throw CserealException(ErrorCode.RESEARCH_GROUP_NOT_FOUND, mapOf("groupId" to groupId))

    private fun upsertSearchIndex(translation: LabTranslationEntity) {
        translation.researchSearch?.update(translation)
            ?: let { translation.researchSearch = ResearchSearchEntity.create(translation) }
    }

    private fun LabEntity.toLanguageDto(): LabLanguageDto =
        LabLanguageDto.of(this, attachmentService.createOneAttachmentResponse(pdf))
}
