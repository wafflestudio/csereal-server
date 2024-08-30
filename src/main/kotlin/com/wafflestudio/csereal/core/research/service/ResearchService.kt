package com.wafflestudio.csereal.core.research.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.properties.EndpointProperties
import com.wafflestudio.csereal.common.utils.startsWithEnglish
import com.wafflestudio.csereal.core.member.database.ProfessorRepository
import com.wafflestudio.csereal.core.research.api.req.*
import com.wafflestudio.csereal.core.research.database.*
import com.wafflestudio.csereal.core.research.dto.*
import com.wafflestudio.csereal.core.research.type.ResearchRelatedType
import com.wafflestudio.csereal.core.research.type.ResearchType
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.resource.attachment.service.AttachmentService
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface ResearchService {
    fun createResearchLanguage(req: CreateResearchLanguageReqBody, mainImage: MultipartFile?): ResearchLanguageDto
    fun createResearch(
        language: LanguageType,
        request: CreateResearchSealedReqBody,
        mainImage: MultipartFile?
    ): ResearchSealedDto

    fun updateResearchLanguage(
        koreanId: Long,
        englishId: Long,
        req: ModifyResearchLanguageReqBody,
        updateImage: MultipartFile?
    ): ResearchLanguageDto

    fun updateResearch(
        researchId: Long,
        request: ModifyResearchSealedReqBody,
        updateImage: MultipartFile?
    ): ResearchSealedDto

    fun deleteResearchLanguage(koreanId: Long, englishId: Long)
    fun deleteResearch(researchId: Long)

    fun readResearchLanguage(id: Long): ResearchLanguageDto
    fun readAllResearch(language: LanguageType, type: ResearchType): List<ResearchSealedDto>

    fun readAllResearchGroupsDeprecated(language: String): ResearchGroupResponse
    fun readAllResearchCentersDeprecated(language: String): List<ResearchDto>

    fun createLab(request: LabDto, pdf: MultipartFile?): LabDto
    fun readAllLabs(language: String): List<LabDto>
    fun readLab(labId: Long): LabDto
    fun updateLab(labId: Long, request: LabUpdateRequest, pdf: MultipartFile?): LabDto
}

@Service
class ResearchServiceImpl(
    private val researchRepository: ResearchRepository,
    private val researchLanguageRepository: ResearchLanguageRepository,
    private val labRepository: LabRepository,
    private val professorRepository: ProfessorRepository,
    private val mainImageService: MainImageService,
    private val attachmentService: AttachmentService,
    private val endpointProperties: EndpointProperties
) : ResearchService {
    @Transactional
    override fun createResearchLanguage(
        req: CreateResearchLanguageReqBody,
        mainImage: MultipartFile?,
    ): ResearchLanguageDto {
        if (!req.valid()) {
            throw CserealException.Csereal400("두 언어의 research type이 일치하지 않습니다.")
        }

        val ko = createResearch(LanguageType.KO, req.ko, mainImage)
        val en = createResearch(LanguageType.EN, req.en, mainImage)
        researchLanguageRepository.save(
            ResearchLanguageEntity(
                koreanId = ko.id,
                englishId = en.id,
                type = req.ko.type.ofResearchRelatedType(),
            )
        )

        return ResearchLanguageDto(ko, en)
    }

    @Transactional
    override fun createResearch(
        language: LanguageType,
        request: CreateResearchSealedReqBody,
        mainImage: MultipartFile?,
    ): ResearchSealedDto {
        // Common fields
        val newResearch = ResearchEntity(
            postType = request.type,
            language = language,
            name = request.name,
            description = request.description,
        )

        // Type specific fields
        when (request) {
            is CreateResearchGroupReqBody -> {}
            is CreateResearchCenterReqBody -> newResearch.websiteURL = request.websiteURL
        }

        // Create Research Search Index
        upsertResearchSearchIndex(newResearch)

        // Main Image
        if (mainImage != null) {
            mainImageService.uploadMainImage(newResearch, mainImage)
        }
        val imageURL = mainImageService.createImageURL(newResearch.mainImage)

        return ResearchSealedDto.of(
            researchRepository.save(newResearch),
            imageURL,
        )
    }

    @Transactional
    override fun updateResearchLanguage(
        koreanId: Long,
        englishId: Long,
        req: ModifyResearchLanguageReqBody,
        updateImage: MultipartFile?,
    ): ResearchLanguageDto {
        if (!req.valid()) {
            throw CserealException.Csereal404("두 언어의 research type이 일치하지 않습니다.")
        }

        val type = req.ko.type
        if (!researchLanguageRepository.existsByKoreanIdAndEnglishIdAndType(
                koreanId,
                englishId,
                type.ofResearchRelatedType()
            )
        ) {
            throw CserealException.Csereal404("해당 Research 언어 쌍을 찾을 수 없습니다.")
        }

        val koreanUpdatedDto = updateResearch(koreanId, req.ko, updateImage)
        val englishUpdatedDto = updateResearch(englishId, req.en, updateImage)

        return ResearchLanguageDto(koreanUpdatedDto, englishUpdatedDto)
    }

    @Transactional
    override fun updateResearch(
        researchId: Long,
        request: ModifyResearchSealedReqBody,
        updateImage: MultipartFile?,
    ): ResearchSealedDto {
        val research = researchRepository.findByIdOrNull(researchId)
            ?: throw CserealException.Csereal404("해당 게시글을 찾을 수 없습니다.(researchId=$researchId)")
        val originalName = research.name

        // Update common fields
        research.apply {
            name = request.name
            description = request.description
        }

        // Update type specific fields
        when (request) {
            is ModifyResearchGroupReqBody -> {}
            is ModifyResearchCenterReqBody -> {
                research.websiteURL = request.websiteURL
            }
        }

        // Update image
        // remove old image
        if (research.mainImage != null && (request.removeImage || updateImage != null)) {
            mainImageService.removeImage(research.mainImage!!)
            research.mainImage = null
        }
        // upload new image
        updateImage?.let {
            mainImageService.uploadMainImage(research, it)
        }
        val imageURL = mainImageService.createImageURL(research.mainImage)

        // update search index
        upsertResearchSearchIndex(research)

        // upsert labs in research group if name changed
        if (originalName != research.name) {
            research.labs.forEach {
                upsertLabSearchIndex(it)
            }
        }

        return ResearchSealedDto.of(research, imageURL)
    }

    @Transactional
    override fun deleteResearchLanguage(koreanId: Long, englishId: Long) {
        val researchLanguage = researchLanguageRepository.findByKoreanIdAndEnglishIdAndType(
            koreanId,
            englishId,
            ResearchRelatedType.RESEARCH_GROUP
        ) ?: researchLanguageRepository.findByKoreanIdAndEnglishIdAndType(
            koreanId,
            englishId,
            ResearchRelatedType.RESEARCH_CENTER
        ) ?: throw CserealException.Csereal404("해당 Research 언어 쌍을 찾을 수 없습니다.")

        deleteResearch(koreanId)
        deleteResearch(englishId)
        researchLanguageRepository.delete(researchLanguage)
    }

    @Transactional
    override fun deleteResearch(researchId: Long) {
        val research = researchRepository.findByIdOrNull(researchId)
            ?: throw CserealException.Csereal404("해당 게시글을 찾을 수 없습니다.(researchId=$researchId)")

        research.mainImage?.let {
            mainImageService.removeImage(it)
        }

        research.labs.forEach {
            it.research = null
        }

        // update search index to remove research
        research.labs.forEach {
            upsertLabSearchIndex(it)
        }

        researchRepository.delete(research)
    }

    @Transactional(readOnly = true)
    override fun readResearchLanguage(id: Long): ResearchLanguageDto {
        val researchMap = researchLanguageRepository.findResearchPairById(id)
            ?: throw CserealException.Csereal404("해당 Research 언어 쌍을 찾을 수 없습니다.(id=$id)")

        val ko = researchMap[LanguageType.KO]!!
        val en = researchMap[LanguageType.EN]!!
        return ResearchLanguageDto(
            ResearchSealedDto.of(ko, mainImageService.createImageURL(ko.mainImage)),
            ResearchSealedDto.of(en, mainImageService.createImageURL(en.mainImage))
        )
    }

    @Transactional(readOnly = true)
    override fun readAllResearch(language: LanguageType, type: ResearchType): List<ResearchSealedDto> =
        researchRepository.findAllByPostTypeAndLanguageOrderByName(type, language)
            .map { ResearchSealedDto.of(it, mainImageService.createImageURL(it.mainImage)) }


    @Transactional(readOnly = true)
    override fun readAllResearchGroupsDeprecated(language: String): ResearchGroupResponse {
        // Todo: description 수정 필요
        val description = "세계가 주목하는 컴퓨터공학부의 많은 교수들은 ACM, IEEE 등 " +
            "세계적인 컴퓨터관련 주요 학회에서 국제학술지 편집위원, 국제학술회의 위원장, " +
            "기조연설자 등으로 활발하게 활동하고 있습니다. 정부 지원과제, 민간 산업체 지원 " +
            "연구과제 등도 성공적으로 수행, 우수한 성과들을 내놓고 있으며, 오늘도 인류가 " +
            "꿈꾸는 행복하고 편리한 세상을 위해 변화와 혁신, 연구와 도전을 계속하고 있습니다."

        val enumLanguageType = LanguageType.makeStringToLanguageType(language)
        val researchGroups =
            researchRepository.findAllByPostTypeAndLanguageOrderByName(
                ResearchType.GROUPS,
                enumLanguageType
            ).map {
                val imageURL = mainImageService.createImageURL(it.mainImage)
                ResearchDto.of(it, imageURL, emptyList())
            }

        return ResearchGroupResponse(description, researchGroups)
    }

    @Transactional(readOnly = true)
    override fun readAllResearchCentersDeprecated(language: String): List<ResearchDto> {
        val enumLanguageType = LanguageType.makeStringToLanguageType(language)
        val researchCenters =
            researchRepository.findAllByPostTypeAndLanguageOrderByName(
                ResearchType.CENTERS,
                enumLanguageType
            ).map {
                val imageURL = mainImageService.createImageURL(it.mainImage)
                ResearchDto.of(it, imageURL, emptyList())
            }

        return researchCenters
    }


    @Transactional
    override fun createLab(request: LabDto, pdf: MultipartFile?): LabDto {
        val researchGroup = researchRepository.findByName(request.group!!)
            ?: throw CserealException.Csereal404("해당 연구그룹을 찾을 수 없습니다.(researchGroupId = ${request.group})")

        if (researchGroup.postType != ResearchType.GROUPS) {
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
        }.sortedWith { a, b ->
            when {
                startsWithEnglish(a.name) && !startsWithEnglish(b.name) -> 1
                !startsWithEnglish(a.name) && startsWithEnglish(b.name) -> -1
                else -> a.name.compareTo(b.name)
            }
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

    // TODO: professor search update
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
            labEntity.pdf?.let { attachmentService.deleteAttachmentDeprecated(it) }

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
    fun upsertResearchSearchIndex(research: ResearchEntity) {
        research.researchSearch?.update(research) ?: let {
            research.researchSearch = ResearchSearchEntity.create(research)
        }
    }

    @Transactional
    fun upsertLabSearchIndex(lab: LabEntity) {
        lab.researchSearch?.update(lab) ?: let {
            lab.researchSearch = ResearchSearchEntity.create(lab)
        }
    }
}
