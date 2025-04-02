package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.utils.startsWithEnglish
import com.wafflestudio.csereal.core.member.api.req.CreateProfessorLanguagesReqBody
import com.wafflestudio.csereal.core.member.api.req.CreateProfessorReqBody
import com.wafflestudio.csereal.core.member.api.req.ModifyProfessorLanguagesReqBody
import com.wafflestudio.csereal.core.member.api.req.ModifyProfessorReqBody
import com.wafflestudio.csereal.core.member.database.*
import com.wafflestudio.csereal.core.member.dto.ProfessorDto
import com.wafflestudio.csereal.core.member.dto.ProfessorLanguagesDto
import com.wafflestudio.csereal.core.member.dto.ProfessorPageDto
import com.wafflestudio.csereal.core.member.dto.SimpleProfessorDto
import com.wafflestudio.csereal.core.member.event.ProfessorCreatedEvent
import com.wafflestudio.csereal.core.member.event.ProfessorDeletedEvent
import com.wafflestudio.csereal.core.member.event.ProfessorModifiedEvent
import com.wafflestudio.csereal.core.member.type.MemberType
import com.wafflestudio.csereal.core.research.database.LabRepository
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface ProfessorService {
    fun getProfessor(professorId: Long): ProfessorDto
    fun getProfessorLanguages(professorId: Long): ProfessorLanguagesDto
    fun getActiveProfessors(language: String): ProfessorPageDto
    fun getInactiveProfessors(language: String): List<SimpleProfessorDto>

    fun createProfessor(
        language: LanguageType,
        createProfessorRequest: CreateProfessorReqBody,
        mainImage: MultipartFile?
    ): ProfessorDto

    fun createProfessorLanguages(
        req: CreateProfessorLanguagesReqBody,
        mainImage: MultipartFile?
    ): ProfessorLanguagesDto

    fun updateProfessor(
        professorId: Long,
        updateProfessorRequest: ModifyProfessorReqBody,
        mainImage: MultipartFile?
    ): ProfessorDto

    fun updateProfessorLanguages(
        koProfessorId: Long,
        enProfessorId: Long,
        req: ModifyProfessorLanguagesReqBody,
        newImage: MultipartFile?
    ): ProfessorLanguagesDto

    fun deleteProfessor(professorId: Long)
    fun deleteProfessorLanguages(koProfessorId: Long, enProfessorId: Long)
}

@Service
@Transactional
class ProfessorServiceImpl(
    private val memberLanguageRepository: MemberLanguageRepository,
    private val labRepository: LabRepository,
    private val professorRepository: ProfessorRepository,
    private val mainImageService: MainImageService,
    private val applicationEventPublisher: ApplicationEventPublisher
) : ProfessorService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional(readOnly = true)
    override fun getProfessor(professorId: Long): ProfessorDto {
        val professor = professorRepository.findByIdOrNull(professorId)
            ?: throw CserealException.Csereal404("해당 교수님을 찾을 수 없습니다. professorId: $professorId")

        val imageURL = mainImageService.createImageURL(professor.mainImage)

        return ProfessorDto.of(professor, imageURL)
    }

    override fun getProfessorLanguages(professorId: Long): ProfessorLanguagesDto {
        val professors = professorRepository.findProfessorAllLanguages(professorId)
        if (professors.isEmpty()) {
            throw CserealException.Csereal404("해당 교수님을 찾을 수 없습니다. professorId: $professorId")
        }

        if (professors.any { it.value.size > 1 }) {
            logger.error("professor 데이터 정합성 오류: $professorId")
        }

        return ProfessorLanguagesDto(
            ko = professors[LanguageType.KO]?.let {
                ProfessorDto.of(
                    it.first(),
                    mainImageService.createImageURL(it.first().mainImage)
                )
            },
            en = professors[LanguageType.EN]?.let {
                ProfessorDto.of(
                    it.first(),
                    mainImageService.createImageURL(it.first().mainImage)
                )
            }
        )
    }

    @Transactional(readOnly = true)
    override fun getActiveProfessors(language: String): ProfessorPageDto {
        val enumLanguageType = LanguageType.makeStringToLanguageType(language)

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

        val professors = professorRepository.findByLanguageAndStatusNot(
            enumLanguageType,
            ProfessorStatus.INACTIVE
        ).map {
            val imageURL = mainImageService.createImageURL(it.mainImage)
            SimpleProfessorDto.of(it, imageURL)
        }.sortedWith { a, b ->

            when {
                enumLanguageType == LanguageType.EN -> {
                    val lastNameA = a.name.split(" ").last()
                    val lastNameB = b.name.split(" ").last()
                    lastNameA.compareTo(lastNameB)
                }

                startsWithEnglish(a.name) && !startsWithEnglish(b.name) -> 1
                !startsWithEnglish(a.name) && startsWithEnglish(b.name) -> -1
                else -> a.name.compareTo(b.name)
            }
        }

        return ProfessorPageDto(description, professors)
    }

    @Transactional(readOnly = true)
    override fun getInactiveProfessors(language: String): List<SimpleProfessorDto> {
        val enumLanguageType = LanguageType.makeStringToLanguageType(language)
        return professorRepository.findByLanguageAndStatus(
            enumLanguageType,
            ProfessorStatus.INACTIVE
        ).map {
            val imageURL = mainImageService.createImageURL(it.mainImage)
            SimpleProfessorDto.of(it, imageURL)
        }.sortedWith { a, b ->
            when {
                enumLanguageType == LanguageType.EN -> {
                    val lastNameA = a.name.split(" ").last()
                    val lastNameB = b.name.split(" ").last()
                    lastNameA.compareTo(lastNameB)
                }

                startsWithEnglish(a.name) && !startsWithEnglish(b.name) -> 1
                !startsWithEnglish(a.name) && startsWithEnglish(b.name) -> -1
                else -> a.name.compareTo(b.name)
            }
        }
    }

    override fun createProfessor(
        language: LanguageType,
        createProfessorRequest: CreateProfessorReqBody,
        mainImage: MultipartFile?
    ): ProfessorDto {
        val professor = createProfessorRequest.run {
            ProfessorEntity(
                language = language,
                name = name,
                status = status,
                academicRank = academicRank,
                startDate = startDate,
                endDate = endDate,
                office = office,
                phone = phone,
                fax = fax,
                email = email,
                website = website,
                careers = careers.toMutableList()
            )
        }

        if (createProfessorRequest.labId != null) {
            val lab = labRepository.findByIdOrNull(createProfessorRequest.labId)
                ?: throw CserealException.Csereal404("해당 연구실을 찾을 수 없습니다. LabId: ${createProfessorRequest.labId}")
            professor.addLab(lab)
        }

        for (education in createProfessorRequest.educations) {
            EducationEntity.create(education, professor)
        }

        for (researchArea in createProfessorRequest.researchAreas) {
            ResearchAreaEntity.create(researchArea, professor)
        }

        if (mainImage != null) {
            mainImageService.uploadMainImage(professor, mainImage)
        }

        professor.memberSearch = MemberSearchEntity.create(professor)

        professorRepository.save(professor)

        val imageURL = mainImageService.createImageURL(professor.mainImage)

        applicationEventPublisher.publishEvent(
            ProfessorCreatedEvent.of(professor)
        )

        return ProfessorDto.of(professor, imageURL)
    }

    override fun createProfessorLanguages(
        req: CreateProfessorLanguagesReqBody,
        mainImage: MultipartFile?
    ): ProfessorLanguagesDto {
        val koreanProfessorDto = createProfessor(LanguageType.KO, req.ko, mainImage)
        val englishProfessorDto = createProfessor(LanguageType.EN, req.ko, mainImage)

        memberLanguageRepository.save(
            MemberLanguageEntity(
                MemberType.PROFESSOR,
                koreanId = koreanProfessorDto.id,
                englishId = englishProfessorDto.id
            )
        )

        return ProfessorLanguagesDto(koreanProfessorDto, englishProfessorDto)
    }

    override fun updateProfessor(
        professorId: Long,
        updateReq: ModifyProfessorReqBody,
        newImage: MultipartFile?
    ): ProfessorDto {
        val professor = professorRepository.findByIdOrNull(professorId)
            ?: throw CserealException.Csereal404("해당 교수님을 찾을 수 없습니다. professorId: $professorId")

        // Lab 업데이트
        // 기존 연구실이 제거되지 않는 이상 수동으로 교수의 Lab을 제거할 수 없음
        val outdatedLabId = professor.lab?.id
        if (updateReq.labId != null && updateReq.labId != professor.lab?.id) {
            val lab = labRepository.findByIdOrNull(updateReq.labId) ?: throw CserealException.Csereal404(
                "해당 연구실을 찾을 수 없습니다. LabId: ${updateReq.labId}"
            )
            professor.addLab(lab)
        }

        // 교수 정보 업데이트
        updateReq.let {
            professor.run {
                name = it.name
                status = it.status
                academicRank = it.academicRank
                startDate = it.startDate
                endDate = it.endDate
                office = it.office
                phone = it.phone
                fax = it.fax
                email = it.email
                website = it.website
                careers = it.careers.toMutableList()
            }
        }

        // Main Image 업데이트
        if (updateReq.removeImage && newImage == null) {
            if (professor.mainImage != null) {
                mainImageService.removeImage(professor.mainImage!!)
                professor.mainImage = null
            }
        } else if (newImage != null) {
            professor.mainImage?.let {
                mainImageService.removeImage(it)
            }
            mainImageService.uploadMainImage(professor, newImage)
        }

        // 학력 업데이트
        val oldEducations = professor.educations.map { it.name }

        val educationsToRemove = oldEducations - updateReq.educations
        val educationsToAdd = updateReq.educations - oldEducations
        professor.educations.removeIf { it.name in educationsToRemove }
        for (education in educationsToAdd) {
            EducationEntity.create(education, professor)
        }

        // 연구 분야 업데이트
        val oldResearchAreas = professor.researchAreas.map { it.name }
        val researchAreasToRemove = oldResearchAreas - updateReq.researchAreas
        val researchAreasToAdd = updateReq.researchAreas - oldResearchAreas
        professor.researchAreas.removeIf { it.name in researchAreasToRemove }
        for (researchArea in researchAreasToAdd) {
            ResearchAreaEntity.create(researchArea, professor)
        }

        // 검색 엔티티 업데이트
        professor.memberSearch!!.update(professor)

        // update event 생성
        applicationEventPublisher.publishEvent(
            ProfessorModifiedEvent.of(professor, outdatedLabId)
        )

        val imageURL = mainImageService.createImageURL(professor.mainImage)
        return ProfessorDto.of(professor, imageURL)
    }

    override fun updateProfessorLanguages(
        koProfessorId: Long,
        enProfessorId: Long,
        req: ModifyProfessorLanguagesReqBody,
        newImage: MultipartFile?
    ): ProfessorLanguagesDto {
        // check given id is paired
        if (!memberLanguageRepository.existsByKoreanIdAndEnglishIdAndType(
                koProfessorId,
                enProfessorId,
                MemberType.PROFESSOR
            )
        ) {
            throw CserealException.Csereal404("해당 교수 쌍을 찾을 수 없스빈다. <$koProfessorId, $enProfessorId>")
        }

        val koProfessorDto = updateProfessor(koProfessorId, req.ko, newImage)
        val enProfessorDto = updateProfessor(enProfessorId, req.en, newImage)
        return ProfessorLanguagesDto(koProfessorDto, enProfessorDto)
    }

    override fun deleteProfessor(professorId: Long) {
        val professorEntity = professorRepository.findByIdOrNull(professorId) ?: return

        professorEntity.mainImage?.let {
            mainImageService.removeImage(it)
        }

        professorRepository.delete(professorEntity)

        applicationEventPublisher.publishEvent(
            ProfessorDeletedEvent.of(professorEntity)
        )
    }

    override fun deleteProfessorLanguages(koProfessorId: Long, enProfessorId: Long) {
        deleteProfessor(koProfessorId)
        deleteProfessor(enProfessorId)

        memberLanguageRepository.findByKoreanIdAndEnglishIdAndType(koProfessorId, enProfessorId, MemberType.PROFESSOR)
            ?.let { memberLanguageRepository.delete(it) }
            ?: throw CserealException.Csereal404("해당 교수 쌍을 찾을 수 없습니다. <$koProfessorId, $enProfessorId>")
    }
}
