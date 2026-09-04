package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.utils.startsWithEnglish
import com.wafflestudio.csereal.core.member.api.req.CreateProfessorLanguagesReqBody
import com.wafflestudio.csereal.core.member.api.req.ModifyProfessorLanguagesReqBody
import com.wafflestudio.csereal.core.member.database.MemberSearchEntity
import com.wafflestudio.csereal.core.member.database.ProfessorEntity
import com.wafflestudio.csereal.core.member.database.ProfessorRepository
import com.wafflestudio.csereal.core.member.database.ProfessorStatus
import com.wafflestudio.csereal.core.member.database.ProfessorTranslationEntity
import com.wafflestudio.csereal.core.member.database.ProfessorTranslationRepository
import com.wafflestudio.csereal.core.member.dto.ProfessorLanguagesDto
import com.wafflestudio.csereal.core.member.dto.ProfessorPageDto
import com.wafflestudio.csereal.core.member.dto.SimpleProfessorDto
import com.wafflestudio.csereal.core.member.event.ProfessorCreatedEvent
import com.wafflestudio.csereal.core.member.event.ProfessorDeletedEvent
import com.wafflestudio.csereal.core.member.event.ProfessorModifiedEvent
import com.wafflestudio.csereal.core.research.database.LabRepository
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface ProfessorService {
    fun getProfessorLanguages(professorId: Long): ProfessorLanguagesDto
    fun getActiveProfessors(language: LanguageType): ProfessorPageDto
    fun getInactiveProfessors(language: LanguageType): List<SimpleProfessorDto>
    fun createProfessorLanguages(
        req: CreateProfessorLanguagesReqBody,
        mainImage: MultipartFile?
    ): ProfessorLanguagesDto

    fun updateProfessorLanguages(
        professorId: Long,
        req: ModifyProfessorLanguagesReqBody,
        newImage: MultipartFile?
    ): ProfessorLanguagesDto

    fun deleteProfessorLanguages(professorId: Long)
}

@Service
@Transactional
class ProfessorServiceImpl(
    private val professorRepository: ProfessorRepository,
    private val professorTranslationRepository: ProfessorTranslationRepository,
    private val labRepository: LabRepository,
    private val mainImageService: MainImageService,
    private val applicationEventPublisher: ApplicationEventPublisher
) : ProfessorService {

    override fun createProfessorLanguages(
        req: CreateProfessorLanguagesReqBody,
        mainImage: MultipartFile?
    ): ProfessorLanguagesDto {
        val professor = ProfessorEntity(
            status = req.status,
            startDate = req.startDate,
            endDate = req.endDate,
            phone = req.phone,
            fax = req.fax,
            email = req.email,
            website = req.website
        )
        req.labId?.let { labId ->
            val lab = labRepository.findByIdOrNull(labId)
                ?: throw CserealException(ErrorCode.LAB_NOT_FOUND, mapOf("labId" to labId))
            professor.addLab(lab)
        }

        listOf(LanguageType.KO to req.ko, LanguageType.EN to req.en).forEach { (language, content) ->
            professor.translations.add(
                ProfessorTranslationEntity(
                    professor = professor,
                    language = language,
                    name = content.name,
                    academicRank = content.academicRank,
                    department = content.department,
                    office = content.office,
                    educations = content.educations.map { it.trim() }.toMutableList(),
                    researchAreas = content.researchAreas.map { it.trim() }.toMutableList(),
                    careers = content.careers.map { it.trim() }.toMutableList()
                )
            )
        }

        // 사진은 사람에게 하나뿐이다 — 예전엔 언어별로 한 번씩 올라가 같은 파일이 두 벌 남았다.
        if (mainImage != null) {
            mainImageService.uploadMainImage(professor, mainImage)
        }
        professor.translations.forEach { it.memberSearch = MemberSearchEntity.create(it) }
        professorRepository.save(professor)

        applicationEventPublisher.publishEvent(ProfessorCreatedEvent.of(professor))
        return professor.toLanguagesDto()
    }

    @Transactional(readOnly = true)
    override fun getProfessorLanguages(professorId: Long): ProfessorLanguagesDto {
        val professor = professorRepository.findByIdOrNull(professorId)
            ?: throw CserealException(ErrorCode.PROFESSOR_NOT_FOUND, mapOf("professorId" to professorId))
        return professor.toLanguagesDto()
    }

    @Transactional(readOnly = true)
    override fun getActiveProfessors(language: LanguageType): ProfessorPageDto {
        // TODO: Refactor to save in database
        val description =
            "컴퓨터공학부는 35명의 훌륭한 교수진과 최신 시설을 갖추고 400여 명의 학부생과 " +
                "350여 명의 대학원생에게 세계 최고 수준의 교육 연구 환경을 제공하고 있다. 2005년에는 서울대학교 " +
                "최초로 외국인 정교수인 Robert Ian McKay 교수를 임용한 것을 시작으로 교내에서 가장 국제화가 " +
                "활발하게 이루어지고 있는 학부로 평가받고 있다. 현재 훌륭한 외국인 교수님 두 분이 학부 학생들의 " +
                "교육 및 연구 지도에 총력을 기울이고 있다.\n\n다수의 외국인 학부생, 대학원생이 재학 중에 있으며 매" +
                " 학기 전공 필수 과목을 비롯한 30% 이상의 과목이 영어로 개설되고 있어 외국인 학생의 학업을 돕는 " +
                "동시에 한국인 학생이 세계로 진출하는 초석이 되고 있다. 또한 CSE int’l Luncheon을 개최하여 " +
                "학부 내 외국인 구성원의 화합과 생활의 불편함을 최소화하는 등 학부 차원에서 최선을 다하고 있다."

        val professors = professorTranslationRepository
            .findAllByLanguageAndProfessorStatusNot(language, ProfessorStatus.INACTIVE)
            .toSimpleDtos(language)

        return ProfessorPageDto(description, professors)
    }

    @Transactional(readOnly = true)
    override fun getInactiveProfessors(language: LanguageType): List<SimpleProfessorDto> =
        professorTranslationRepository
            .findAllByLanguageAndProfessorStatus(language, ProfessorStatus.INACTIVE)
            .toSimpleDtos(language)

    override fun updateProfessorLanguages(
        professorId: Long,
        req: ModifyProfessorLanguagesReqBody,
        newImage: MultipartFile?
    ): ProfessorLanguagesDto {
        val professor = professorRepository.findByIdOrNull(professorId)
            ?: throw CserealException(ErrorCode.PROFESSOR_NOT_FOUND, mapOf("professorId" to professorId))

        val outdatedLabId = professor.lab?.id
        // 기존 연구실은 자동으로 빠지지 않는다 — 새 연구실이 올 때만 교체한다.
        if (req.labId != null && req.labId != outdatedLabId) {
            val lab = labRepository.findByIdOrNull(req.labId)
                ?: throw CserealException(ErrorCode.LAB_NOT_FOUND, mapOf("labId" to req.labId))
            professor.addLab(lab)
        }

        professor.apply {
            status = req.status
            startDate = req.startDate
            endDate = req.endDate
            phone = req.phone
            fax = req.fax
            email = req.email
            website = req.website
        }

        listOf(LanguageType.KO to req.ko, LanguageType.EN to req.en).forEach { (language, content) ->
            val translation = professor.translationOf(language)
                ?: throw CserealException(ErrorCode.PROFESSOR_NOT_FOUND, mapOf("professorId" to professorId))
            translation.name = content.name
            translation.academicRank = content.academicRank
            translation.department = content.department
            translation.office = content.office
            translation.educations = content.educations.map { it.trim() }.toMutableList()
            translation.researchAreas = content.researchAreas.map { it.trim() }.toMutableList()
            translation.careers = content.careers.map { it.trim() }.toMutableList()
            translation.memberSearch?.update(translation)
                ?: let { translation.memberSearch = MemberSearchEntity.create(translation) }
        }

        if (req.removeImage && newImage == null) {
            professor.mainImage?.let {
                mainImageService.removeImage(it)
                professor.mainImage = null
            }
        } else if (newImage != null) {
            professor.mainImage?.let { mainImageService.removeImage(it) }
            mainImageService.uploadMainImage(professor, newImage)
        }

        applicationEventPublisher.publishEvent(ProfessorModifiedEvent.of(professor, outdatedLabId))
        return professor.toLanguagesDto()
    }

    override fun deleteProfessorLanguages(professorId: Long) {
        val professor = professorRepository.findByIdOrNull(professorId)
            ?: throw CserealException(ErrorCode.PROFESSOR_NOT_FOUND, mapOf("professorId" to professorId))

        professor.mainImage?.let { mainImageService.removeImage(it) }
        val event = ProfessorDeletedEvent.of(professor)
        // 번역본과 검색 색인은 cascade + orphanRemoval 로 함께 지워진다.
        professorRepository.delete(professor)
        applicationEventPublisher.publishEvent(event)
    }

    private fun ProfessorEntity.toLanguagesDto(): ProfessorLanguagesDto =
        ProfessorLanguagesDto.of(this, mainImageService.createImageURL(mainImage))

    private fun List<ProfessorTranslationEntity>.toSimpleDtos(language: LanguageType): List<SimpleProfessorDto> =
        map { SimpleProfessorDto.of(it, mainImageService.createImageURL(it.professor.mainImage)) }
            .sortedWith { a, b ->
                when {
                    language == LanguageType.EN ->
                        a.name.split(" ").last().compareTo(b.name.split(" ").last())

                    startsWithEnglish(a.name) && !startsWithEnglish(b.name) -> 1
                    !startsWithEnglish(a.name) && startsWithEnglish(b.name) -> -1
                    else -> a.name.compareTo(b.name)
                }
            }
}
