package com.wafflestudio.csereal.core.research.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.properties.EndpointProperties
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.database.ProfessorRepository
import com.wafflestudio.csereal.core.research.database.*
import com.wafflestudio.csereal.core.research.dto.*
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.resource.attachment.service.AttachmentService
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface ResearchService {
    fun createResearchDetail(
        request: ResearchDto,
        mainImage: MultipartFile?,
        attachments: List<MultipartFile>?
    ): ResearchDto

    fun readAllResearchGroups(language: String): ResearchGroupResponse
    fun readAllResearchCenters(language: String): List<ResearchDto>
    fun updateResearchDetail(
        researchId: Long,
        request: ResearchDto,
        mainImage: MultipartFile?,
        attachments: List<MultipartFile>?
    ): ResearchDto

    fun createLab(request: LabDto, pdf: MultipartFile?): LabDto
    fun readAllLabs(language: String): List<LabDto>
    fun readLab(labId: Long): LabDto
    fun updateLab(labId: Long, request: LabUpdateRequest, pdf: MultipartFile?): LabDto
    fun migrateResearchDetail(requestList: List<ResearchDto>): List<ResearchDto>
    fun migrateLabs(requestList: List<LabDto>): List<LabDto>
    fun migrateResearchDetailImageAndAttachments(
        researchId: Long,
        mainImage: MultipartFile?,
        attachments: List<MultipartFile>?
    ): ResearchDto
    fun migrateLabPdf(labId: Long, pdf: MultipartFile?): LabDto
}

@Service
class ResearchServiceImpl(
    private val researchRepository: ResearchRepository,
    private val labRepository: LabRepository,
    private val professorRepository: ProfessorRepository,
    private val mainImageService: MainImageService,
    private val attachmentService: AttachmentService,
    private val endpointProperties: EndpointProperties
) : ResearchService {
    @Transactional
    override fun createResearchDetail(
        request: ResearchDto,
        mainImage: MultipartFile?,
        attachments: List<MultipartFile>?
    ): ResearchDto {
        val enumLanguageType = LanguageType.makeStringToLanguageType(request.language)
        val newResearch = ResearchEntity.of(enumLanguageType, request)

        if (request.labs != null) {
            for (lab in request.labs) {
                val labEntity = labRepository.findByIdOrNull(lab.id)
                    ?: throw CserealException.Csereal404("해당 연구실을 찾을 수 없습니다.(labId=${lab.id})")
                newResearch.labs.add(labEntity)
                labEntity.research = newResearch
            }
        }

        if (mainImage != null) {
            mainImageService.uploadMainImage(newResearch, mainImage)
        }

        if (attachments != null) {
            attachmentService.uploadAllAttachments(newResearch, attachments)
        }

        newResearch.researchSearch = ResearchSearchEntity.create(newResearch)

        researchRepository.save(newResearch)

        val imageURL = mainImageService.createImageURL(newResearch.mainImage)
        val attachmentResponses =
            attachmentService.createAttachmentResponses(newResearch.attachments)

        return ResearchDto.of(newResearch, imageURL, attachmentResponses)
    }

    @Transactional(readOnly = true)
    override fun readAllResearchGroups(language: String): ResearchGroupResponse {
        // Todo: description 수정 필요
        val description = "세계가 주목하는 컴퓨터공학부의 많은 교수들은 ACM, IEEE 등 " +
            "세계적인 컴퓨터관련 주요 학회에서 국제학술지 편집위원, 국제학술회의 위원장, " +
            "기조연설자 등으로 활발하게 활동하고 있습니다. 정부 지원과제, 민간 산업체 지원 " +
            "연구과제 등도 성공적으로 수행, 우수한 성과들을 내놓고 있으며, 오늘도 인류가 " +
            "꿈꾸는 행복하고 편리한 세상을 위해 변화와 혁신, 연구와 도전을 계속하고 있습니다."

        val enumLanguageType = LanguageType.makeStringToLanguageType(language)
        val researchGroups =
            researchRepository.findAllByPostTypeAndLanguageOrderByName(
                ResearchPostType.GROUPS,
                enumLanguageType
            ).map {
                val imageURL = mainImageService.createImageURL(it.mainImage)
                val attachmentResponses = attachmentService.createAttachmentResponses(it.attachments)

                ResearchDto.of(it, imageURL, attachmentResponses)
            }

        return ResearchGroupResponse(description, researchGroups)
    }

    @Transactional(readOnly = true)
    override fun readAllResearchCenters(language: String): List<ResearchDto> {
        val enumLanguageType = LanguageType.makeStringToLanguageType(language)
        val researchCenters =
            researchRepository.findAllByPostTypeAndLanguageOrderByName(
                ResearchPostType.CENTERS,
                enumLanguageType
            ).map {
                val imageURL = mainImageService.createImageURL(it.mainImage)
                val attachmentResponses = attachmentService.createAttachmentResponses(it.attachments)

                ResearchDto.of(it, imageURL, attachmentResponses)
            }

        return researchCenters
    }

    @Transactional
    override fun updateResearchDetail(
        researchId: Long,
        request: ResearchDto,
        mainImage: MultipartFile?,
        attachments: List<MultipartFile>?
    ): ResearchDto {
        val research = researchRepository.findByIdOrNull(researchId)
            ?: throw CserealException.Csereal404("해당 게시글을 찾을 수 없습니다.(researchId=$researchId)")

        if (request.labs != null) {
            for (lab in request.labs) {
                val labEntity = labRepository.findByIdOrNull(lab.id)
                    ?: throw CserealException.Csereal404("해당 연구실을 찾을 수 없습니다.(labId=${lab.id})")
            }

            val oldLabs = research.labs.map { it.id }

            val labsToRemove = oldLabs - request.labs.map { it.id }
            val labsToAdd = request.labs.map { it.id } - oldLabs

            research.labs.removeIf { it.id in labsToRemove }

            for (labsToAddId in labsToAdd) {
                val lab = labRepository.findByIdOrNull(labsToAddId)!!
                research.labs.add(lab)
                lab.research = research
            }
        }

        if (mainImage != null) {
            mainImageService.uploadMainImage(research, mainImage)
        } else {
            research.mainImage = null
        }

        if (attachments != null) {
            research.attachments.clear()
            attachmentService.uploadAllAttachments(research, attachments)
        } else {
            research.attachments.clear()
        }

        val imageURL = mainImageService.createImageURL(research.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(research.attachments)

        research.updateWithoutLabImageAttachment(request)

        research.researchSearch?.update(research)
            ?: let {
                research.researchSearch = ResearchSearchEntity.create(research)
            }

        return ResearchDto.of(research, imageURL, attachmentResponses)
    }

    @Transactional
    override fun createLab(request: LabDto, pdf: MultipartFile?): LabDto {
        val researchGroup = researchRepository.findByName(request.group!!)
            ?: throw CserealException.Csereal404("해당 연구그룹을 찾을 수 없습니다.(researchGroupId = ${request.group})")

        if (researchGroup.postType != ResearchPostType.GROUPS) {
            throw CserealException.Csereal404("해당 게시글은 연구그룹이어야 합니다.")
        }

        val enumLanguageType = LanguageType.makeStringToLanguageType(request.language)
        val newLab = LabEntity.of(enumLanguageType, request, researchGroup)

        if (request.professors != null) {
            for (professor in request.professors) {
                val professorEntity = professorRepository.findByIdOrNull(professor.id)
                    ?: throw CserealException.Csereal404("해당 교수님을 찾을 수 없습니다.(professorId = ${professor.id}")

                newLab.professors.add(professorEntity)
                professorEntity.lab = newLab
            }
        }

        if (pdf != null) {
            attachmentService.uploadAttachmentInLabEntity(newLab, pdf)
        }

        newLab.researchSearch = ResearchSearchEntity.create(newLab)

        labRepository.save(newLab)

        val attachmentResponse =
            attachmentService.createOneAttachmentResponse(newLab.pdf)

        return LabDto.of(newLab, attachmentResponse)
    }

    @Transactional(readOnly = true)
    override fun readAllLabs(language: String): List<LabDto> {
        val enumLanguageType = LanguageType.makeStringToLanguageType(language)
        val labs = labRepository.findAllByLanguageOrderByName(enumLanguageType).map {
            val attachmentResponse =
                attachmentService.createOneAttachmentResponse(it.pdf)
            LabDto.of(it, attachmentResponse)
        }

        return labs
    }

    @Transactional
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
    override fun updateLab(labId: Long, request: LabUpdateRequest, pdf: MultipartFile?): LabDto {
        val labEntity = labRepository.findByIdOrNull(labId)
            ?: throw CserealException.Csereal404("해당 연구실을 찾을 수 없습니다.(labId=$labId)")

        labEntity.updateWithoutProfessor(request)

        // update professor
        val removedProfessorIds = labEntity.professors.map { it.id } - request.professorIds
        val addedProfessorIds = request.professorIds - labEntity.professors.map { it.id }

        removedProfessorIds.forEach {
            val professor = professorRepository.findByIdOrNull(it)
                ?: throw CserealException.Csereal404("해당 교수님을 찾을 수 없습니다.(professorId=$it)")
            labEntity.professors.remove(
                professor
            )
            professor.lab = null
        }

        addedProfessorIds.forEach {
            val professor = professorRepository.findByIdOrNull(it)
                ?: throw CserealException.Csereal404("해당 교수님을 찾을 수 없습니다.(professorId=$it)")
            labEntity.professors.add(
                professor
            )
            professor.lab = labEntity
        }

        // update pdf
        if (request.pdfModified) {
            labEntity.pdf?.let { attachmentService.deleteAttachment(it) }

            pdf?.let {
                val attachmentDto = attachmentService.uploadAttachmentInLabEntity(labEntity, it)
            }
        }

        // update researchSearch
        labEntity.researchSearch?.update(labEntity)
            ?: let {
                labEntity.researchSearch = ResearchSearchEntity.create(labEntity)
            }

        val attachmentResponse =
            attachmentService.createOneAttachmentResponse(labEntity.pdf)

        return LabDto.of(labEntity, attachmentResponse)
    }

    @Transactional
    override fun migrateResearchDetail(requestList: List<ResearchDto>): List<ResearchDto> {
        val list = mutableListOf<ResearchDto>()
        for (request in requestList) {
            val enumLanguageType = LanguageType.makeStringToLanguageType(request.language)
            val newResearch = ResearchEntity.of(enumLanguageType, request)

            newResearch.researchSearch = ResearchSearchEntity.create(newResearch)

            researchRepository.save(newResearch)

            list.add(ResearchDto.of(newResearch, null, listOf()))
        }

        return list
    }

    @Transactional
    override fun migrateLabs(requestList: List<LabDto>): List<LabDto> {
        val list = mutableListOf<LabDto>()
        for (request in requestList) {
            val researchGroup = researchRepository.findByName(request.group)
                ?: throw CserealException.Csereal404("해당 연구그룹을 찾을 수 없습니다.(researchGroupName = ${request.group})")

            if (researchGroup.postType != ResearchPostType.GROUPS) {
                throw CserealException.Csereal404("해당 게시글은 연구그룹이어야 합니다.")
            }

            val enumLanguageType = LanguageType.makeStringToLanguageType(request.language)
            val newLab = LabEntity.of(enumLanguageType, request, researchGroup)

            newLab.researchSearch = ResearchSearchEntity.create(newLab)

            labRepository.save(newLab)

            list.add(LabDto.of(newLab, null))
        }
        return list
    }

    @Transactional
    override fun migrateResearchDetailImageAndAttachments(
        researchId: Long,
        mainImage: MultipartFile?,
        attachments: List<MultipartFile>?
    ): ResearchDto {
        val researchDetail = researchRepository.findByIdOrNull(researchId)
            ?: throw CserealException.Csereal404("해당 연구내용을 찾을 수 없습니다.")

        if (mainImage != null) {
            mainImageService.uploadMainImage(researchDetail, mainImage)
        }

        if (attachments != null) {
            attachmentService.uploadAllAttachments(researchDetail, attachments)
        }

        val imageURL = mainImageService.createImageURL(researchDetail.mainImage)
        val attachmentResponses = attachmentService.createAttachmentResponses(researchDetail.attachments)

        return ResearchDto.of(researchDetail, imageURL, attachmentResponses)
    }

    @Transactional
    override fun migrateLabPdf(labId: Long, pdf: MultipartFile?): LabDto {
        val lab = labRepository.findByIdOrNull(labId)
            ?: throw CserealException.Csereal404("해당 연구실을 찾을 수 없습니다.")

        if (pdf != null) {
            val attachmentDto = attachmentService.uploadAttachmentInLabEntity(lab, pdf)
        }

        val attachmentResponse =
            attachmentService.createOneAttachmentResponse(lab.pdf)

        return LabDto.of(lab, attachmentResponse)
    }
}
