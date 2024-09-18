package com.wafflestudio.csereal.core.research.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.properties.EndpointProperties
import com.wafflestudio.csereal.common.utils.startsWithEnglish
import com.wafflestudio.csereal.core.member.database.ProfessorRepository
import com.wafflestudio.csereal.core.research.api.req.CreateLabLanguageReqBody
import com.wafflestudio.csereal.core.research.api.req.CreateLabReqBody
import com.wafflestudio.csereal.core.research.api.req.ModifyLabLanguageReqBody
import com.wafflestudio.csereal.core.research.api.req.ModifyLabReqBody
import com.wafflestudio.csereal.core.research.database.*
import com.wafflestudio.csereal.core.research.dto.LabDto
import com.wafflestudio.csereal.core.research.dto.LabLanguageDto
import com.wafflestudio.csereal.core.research.event.LabCreatedEvent
import com.wafflestudio.csereal.core.research.event.LabDeletedEvent
import com.wafflestudio.csereal.core.research.event.LabModifiedEvent
import com.wafflestudio.csereal.core.research.type.ResearchRelatedType
import com.wafflestudio.csereal.core.research.type.ResearchType
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.resource.attachment.service.AttachmentService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface LabService {
    fun readLabLanguage(labId: Long): LabLanguageDto
    fun readLab(labId: Long): LabDto
    fun readAllLabs(language: String): List<LabDto>

    fun createLab(language: LanguageType, request: CreateLabReqBody, pdf: MultipartFile?): LabDto
    fun createLabLanguage(request: CreateLabLanguageReqBody, pdf: MultipartFile?): LabLanguageDto

    fun updateLabLanguage(
        koreanId: Long,
        englishId: Long,
        request: ModifyLabLanguageReqBody,
        pdf: MultipartFile?
    ): LabLanguageDto

    fun updateLab(language: LanguageType, labId: Long, request: ModifyLabReqBody, pdf: MultipartFile?): LabDto

    fun deleteLabLanguage(koreanId: Long, englishId: Long)
    fun deleteLab(id: Long)
}

@Service
class LabServiceImpl(
    private val attachmentService: AttachmentService,
    private val researchSearchService: ResearchSearchService,
    private val labRepository: LabRepository,
    private val researchLanguageRepository: ResearchLanguageRepository,
    private val researchRepository: ResearchRepository,
    private val professorRepository: ProfessorRepository,
    private val endpointProperties: EndpointProperties,
    private val applicationEventPublisher: ApplicationEventPublisher,
) : LabService {
    // TODO: Solve N+1 Problem
    @Transactional(readOnly = true)
    override fun readAllLabs(language: String): List<LabDto> {
        val enumLanguageType = LanguageType.makeStringToLanguageType(language)
        val labs = labRepository.findAllByLanguageOrderByName(enumLanguageType).map {
            val attachmentResponse =
                attachmentService.createOneAttachmentResponse(it.pdf)
            LabDto.of(it, attachmentResponse)
        }.sortedWith { a, b ->
            when {
                startsWithEnglish(a.name) && !startsWithEnglish(b.name) -> 1
                !startsWithEnglish(a.name) && startsWithEnglish(b.name) -> -1
                else -> a.name.compareTo(b.name)
            }
        }

        return labs
    }

    @Transactional(readOnly = true)
    override fun readLabLanguage(labId: Long): LabLanguageDto {
        val labMap = researchLanguageRepository.findLabPairById(labId)
            ?.takeIf { it.isNotEmpty() }
            ?: throw CserealException.Csereal404("해당 연구실을 찾을 수 없습니다.(labId=$labId)")

        val ko = labMap[LanguageType.KO]!!
        val en = labMap[LanguageType.EN]!!

        val koAttachmentResponse = attachmentService.createOneAttachmentResponse(ko.pdf)
        val enAttachmentResponse = attachmentService.createOneAttachmentResponse(en.pdf)

        return LabLanguageDto(
            LabDto.of(ko, koAttachmentResponse),
            LabDto.of(en, enAttachmentResponse)
        )
    }

    @Transactional(readOnly = true)
    override fun readLab(labId: Long): LabDto {
        val lab = labRepository.findByIdOrNull(labId)
            ?: throw CserealException.Csereal404("해당 연구실을 찾을 수 없습니다.(labId=$labId)")

        val attachmentResponse =
            attachmentService.createOneAttachmentResponse(lab.pdf)

        return LabDto.of(lab, attachmentResponse)
    }

    private fun createPdfURL(pdf: AttachmentEntity): String {
        return "${endpointProperties.backend}/v1/file/${pdf.filename}"
    }

    @Transactional
    override fun createLabLanguage(request: CreateLabLanguageReqBody, pdf: MultipartFile?): LabLanguageDto {
        val koLabDto = createLab(LanguageType.KO, request.ko, pdf)
        val enLabDto = createLab(LanguageType.EN, request.en, pdf)

        researchLanguageRepository.save(
            ResearchLanguageEntity(
                koreanId = koLabDto.id,
                englishId = enLabDto.id,
                type = ResearchRelatedType.LAB
            )
        )

        return LabLanguageDto(koLabDto, enLabDto)
    }

    @Transactional
    override fun createLab(language: LanguageType, request: CreateLabReqBody, pdf: MultipartFile?): LabDto {
        val researchGroup = request.groupId?.let {
            researchRepository.findByIdOrNull(request.groupId)
                ?: throw CserealException.Csereal404("해당 연구그룹을 찾을 수 없습니다.(researchGroupId = ${it})")
        }?.apply {
            if (this.postType != ResearchType.GROUPS) {
                throw CserealException.Csereal404("해당 id 연구그룹이 아닙니다.(researchGroupId = ${this.id})")
            }
        }

        val professors = professorRepository.findAllById(request.professorIds)
            .also {
                if (it.size < request.professorIds.size) {
                    throw CserealException.Csereal404("해당 교수님들을 찾을 수 없습니다.(professorIds = ${request.professorIds})")
                }
                if (it.any { p -> p.lab != null }) {
                    throw CserealException.Csereal400("이미 다른 연구실에 속한 교수님이 존재합니다.")
                }
            }

        val newLab = LabEntity(
            language = language,
            name = request.name,
            description = request.description,
            acronym = request.acronym,
            location = request.location,
            websiteURL = request.websiteURL,
            tel = request.tel,
            youtube = request.youtube,
            research = researchGroup,
            professors = professors.toMutableSet(),
        ).apply {
            pdf?.let {
                attachmentService.uploadAttachmentInLabEntity(this, it)
            }
        }.also {
            upsertLabSearchIndex(it)
        }

        val newSavedLab = labRepository.save(newLab)

        applicationEventPublisher.publishEvent(
            LabCreatedEvent(
                newSavedLab.id,
                request.groupId,
                request.professorIds,
            )
        )

        return LabDto.of(newSavedLab, attachmentService.createOneAttachmentResponse(newSavedLab.pdf))
    }

    @Transactional
    override fun updateLabLanguage(
        koreanId: Long,
        englishId: Long,
        request: ModifyLabLanguageReqBody,
        pdf: MultipartFile?,
    ): LabLanguageDto {
        val koLabDto = updateLab(LanguageType.KO, koreanId, request.ko, pdf)
        val enLabDto = updateLab(LanguageType.EN, englishId, request.en, pdf)

        return LabLanguageDto(koLabDto, enLabDto)
    }

    @Transactional
    override fun updateLab(
        language: LanguageType,
        labId: Long,
        request: ModifyLabReqBody,
        pdf: MultipartFile?
    ): LabDto {
        val labEntity = labRepository.findByIdAndLanguage(labId, language)
            ?: throw CserealException.Csereal404("해당 연구실을 찾을 수 없습니다.(labId=$labId)")

        val oldGroup = labEntity.research
        val newGroup = request.groupId?.let {
            researchRepository.findByIdAndPostType(it, ResearchType.GROUPS)
                ?: throw CserealException.Csereal404("해당 연구그룹을 찾을 수 없습니다.(researchGroupId = ${it})")
        }

        val oldProfessors = labEntity.professors
        val newProfessors = professorRepository.findAllById(request.professorIds)
            .also {
                if (it.size < request.professorIds.size) {
                    throw CserealException.Csereal404("해당 교수님들을 찾을 수 없습니다.(professorIds = ${request.professorIds})")
                }

                if (!(it.all { p -> p.lab == null || p.lab!!.id == labId })) {
                    throw CserealException.Csereal400("이미 다른 연구실에 속한 교수님이 존재합니다.")
                }
            }

        labEntity.apply {
            name = request.name
            description = request.description
            location = request.location
            tel = request.tel
            acronym = request.acronym
            youtube = request.youtube
            websiteURL = request.websiteURL
            research = newGroup
            professors = newProfessors.toMutableSet()
        }

        // update pdf
        if ((pdf != null || request.removePdf) && (labEntity.pdf != null)) {
            attachmentService.deleteAttachment(labEntity.pdf!!)
        }
        pdf?.let {
            attachmentService.uploadAttachmentInLabEntity(labEntity, it)
        }

        // update researchSearch
        upsertLabSearchIndex(labEntity)

        applicationEventPublisher.publishEvent(
            LabModifiedEvent(
                labId,
                oldGroup?.id to newGroup?.id,
                oldProfessors.map { it.id }.toSet() to request.professorIds,
            )
        )

        return LabDto.of(labEntity, attachmentService.createOneAttachmentResponse(labEntity.pdf))
    }

    @Transactional
    override fun deleteLabLanguage(koreanId: Long, englishId: Long) {
        val labLanguage = researchLanguageRepository.findByKoreanIdAndEnglishIdAndType(
            koreanId,
            englishId,
            ResearchRelatedType.LAB
        ) ?: throw CserealException.Csereal404("해당 연구실 언어 쌍을 찾을 수 없습니다.: koreanId=$koreanId, englishId=$englishId")

        deleteLab(koreanId)
        deleteLab(englishId)
        researchLanguageRepository.delete(labLanguage)
    }

    @Transactional
    override fun deleteLab(id: Long) {
        val lab = labRepository.findByIdOrNull(id)
            ?: throw CserealException.Csereal404("해당 연구실을 찾을 수 없습니다.(labId=$id)")

        applicationEventPublisher.publishEvent(
            LabDeletedEvent(
                lab.id,
                lab.research?.id,
                lab.professors.map { it.id }.toSet()
            )
        )

        lab.pdf?.let { attachmentService.deleteAttachment(it) }

        labRepository.delete(lab)
    }

    @Transactional
    fun upsertLabSearchIndex(lab: LabEntity) {
        lab.researchSearch?.update(lab) ?: let {
            lab.researchSearch = ResearchSearchEntity.create(lab)
        }
    }
}
