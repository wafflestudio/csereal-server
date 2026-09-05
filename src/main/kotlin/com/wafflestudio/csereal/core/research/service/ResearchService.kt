package com.wafflestudio.csereal.core.research.service

import com.wafflestudio.csereal.core.research.database.syncSearch
import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.research.api.req.CreateResearchLanguageReqBody
import com.wafflestudio.csereal.core.research.api.req.ModifyResearchLanguageReqBody
import com.wafflestudio.csereal.core.research.database.ResearchEntity
import com.wafflestudio.csereal.core.research.database.ResearchRepository
import com.wafflestudio.csereal.core.research.database.ResearchSearchEntity
import com.wafflestudio.csereal.core.research.database.ResearchTranslationEntity
import com.wafflestudio.csereal.core.research.database.ResearchTranslationRepository
import com.wafflestudio.csereal.core.research.dto.ResearchLanguageDto
import com.wafflestudio.csereal.core.research.dto.ResearchSealedDto
import com.wafflestudio.csereal.core.research.type.ResearchType
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface ResearchService {
    fun createResearchLanguage(req: CreateResearchLanguageReqBody, mainImage: MultipartFile?): ResearchLanguageDto
    fun updateResearchLanguage(
        researchId: Long,
        req: ModifyResearchLanguageReqBody,
        updateImage: MultipartFile?
    ): ResearchLanguageDto

    fun deleteResearchLanguage(researchId: Long)
    fun readResearchLanguage(id: Long): ResearchLanguageDto
    fun readAllResearch(language: LanguageType, type: ResearchType): List<ResearchSealedDto>
}

@Service
@Transactional
class ResearchServiceImpl(
    private val researchRepository: ResearchRepository,
    private val researchTranslationRepository: ResearchTranslationRepository,
    private val mainImageService: MainImageService
) : ResearchService {

    override fun createResearchLanguage(
        req: CreateResearchLanguageReqBody,
        mainImage: MultipartFile?
    ): ResearchLanguageDto {
        val research = ResearchEntity(
            postType = req.type,
            websiteURL = req.websiteURL
        )
        listOf(LanguageType.KO to req.ko, LanguageType.EN to req.en).forEach { (language, content) ->
            research.translations.add(
                ResearchTranslationEntity(
                    research = research,
                    language = language,
                    name = content.name,
                    description = content.description
                )
            )
        }

        // 대표이미지는 하나뿐이라 한 번만 올린다.
        if (mainImage != null) {
            mainImageService.uploadMainImage(research, mainImage)
        }
        research.translations.forEach { it.researchSearch = ResearchSearchEntity.create(it) }
        researchRepository.save(research)

        return research.toLanguageDto()
    }

    override fun updateResearchLanguage(
        researchId: Long,
        req: ModifyResearchLanguageReqBody,
        updateImage: MultipartFile?
    ): ResearchLanguageDto {
        val research = researchRepository.findByIdOrNull(researchId)
            ?: throw CserealException(ErrorCode.RESEARCH_NOT_FOUND, mapOf("researchId" to researchId))

        research.websiteURL = req.websiteURL

        listOf(LanguageType.KO to req.ko, LanguageType.EN to req.en).forEach { (language, content) ->
            val translation = research.translationOf(language)
                ?: throw CserealException(ErrorCode.RESEARCH_NOT_FOUND, mapOf("researchId" to researchId))
            translation.name = content.name
            translation.description = content.description
            translation.syncSearch()
        }

        mainImageService.replaceMainImage(research, updateImage, req.removeImage)

        return research.toLanguageDto()
    }

    override fun deleteResearchLanguage(researchId: Long) {
        val research = researchRepository.findByIdOrNull(researchId)
            ?: throw CserealException(ErrorCode.RESEARCH_NOT_FOUND, mapOf("researchId" to researchId))

        research.mainImage?.let { mainImageService.removeImage(it) }

        // 딸린 연구실은 남고 소속만 끊는다. 색인도 그에 맞춰 다시 쓴다.
        research.labs.forEach { lab ->
            lab.research = null
            lab.translations.forEach { translation ->
                translation.syncSearch()
            }
        }

        researchRepository.delete(research)
    }

    @Transactional(readOnly = true)
    override fun readResearchLanguage(id: Long): ResearchLanguageDto {
        val research = researchRepository.findByIdOrNull(id)
            ?: throw CserealException(ErrorCode.RESEARCH_NOT_FOUND, mapOf("researchId" to id))
        return research.toLanguageDto()
    }

    @Transactional(readOnly = true)
    override fun readAllResearch(language: LanguageType, type: ResearchType): List<ResearchSealedDto> =
        researchTranslationRepository.findAllByLanguageAndResearchPostTypeOrderByName(language, type)
            .map { ResearchSealedDto.of(it, mainImageService.createImageURL(it.research.mainImage)) }

    private fun ResearchEntity.toLanguageDto(): ResearchLanguageDto =
        ResearchLanguageDto.of(this, mainImageService.createImageURL(mainImage))
}
