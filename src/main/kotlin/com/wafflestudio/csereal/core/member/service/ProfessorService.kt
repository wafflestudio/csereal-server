package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.common.utils.startsWithEnglish
import com.wafflestudio.csereal.core.member.database.*
import com.wafflestudio.csereal.core.member.dto.ProfessorDto
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
    fun createProfessor(createProfessorRequest: ProfessorDto, mainImage: MultipartFile?): ProfessorDto
    fun getProfessor(professorId: Long): ProfessorDto
    fun getActiveProfessors(language: String): ProfessorPageDto
    fun getInactiveProfessors(language: String): List<SimpleProfessorDto>
    fun updateProfessor(
        professorId: Long,
        updateProfessorRequest: ProfessorDto,
        mainImage: MultipartFile?
    ): ProfessorDto

    fun deleteProfessor(professorId: Long)
    fun migrateProfessors(requestList: List<ProfessorDto>): List<ProfessorDto>
    fun migrateProfessorImage(professorId: Long, mainImage: MultipartFile): ProfessorDto
}

@Service
@Transactional
class ProfessorServiceImpl(
    private val labRepository: LabRepository,
    private val professorRepository: ProfessorRepository,
    private val mainImageService: MainImageService,
    private val applicationEventPublisher: ApplicationEventPublisher
) : ProfessorService {
    override fun createProfessor(
        createProfessorRequest: ProfessorDto,
        mainImage: MultipartFile?
    ): ProfessorDto {
        val enumLanguageType = LanguageType.makeStringToLanguageType(createProfessorRequest.language)
        val professor = ProfessorEntity.of(enumLanguageType, createProfessorRequest)
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

        for (career in createProfessorRequest.careers) {
            CareerEntity.create(career, professor)
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

    @Transactional(readOnly = true)
    override fun getProfessor(professorId: Long): ProfessorDto {
        val professor = professorRepository.findByIdOrNull(professorId)
            ?: throw CserealException.Csereal404("해당 교수님을 찾을 수 없습니다. professorId: $professorId")

        val imageURL = mainImageService.createImageURL(professor.mainImage)

        return ProfessorDto.of(professor, imageURL)
    }

    @Transactional(readOnly = true)
    override fun getActiveProfessors(language: String): ProfessorPageDto {
        val enumLanguageType = LanguageType.makeStringToLanguageType(language)
        val description = "컴퓨터공학부는 35명의 훌륭한 교수진과 최신 시설을 갖추고 400여 명의 학부생과 " +
            "350여 명의 대학원생에게 세계 최고 수준의 교육 연구 환경을 제공하고 있다. 2005년에는 서울대학교 " +
            "최초로 외국인 정교수인 Robert Ian McKay 교수를 임용한 것을 시작으로 교내에서 가장 국제화가 " +
            "활발하게 이루어지고 있는 학부로 평가받고 있다. 현재 훌륭한 외국인 교수님 두 분이 학부 학생들의 " +
            "교육 및 연구 지도에 총력을 기울이고 있다.\n\n다수의 외국인 학부생, 대학원생이 재학 중에 있으며 매" +
            " 학기 전공 필수 과목을 비롯한 30% 이상의 과목이 영어로 개설되고 있어 외국인 학생의 학업을 돕는 " +
            "동시에 한국인 학생이 세계로 진출하는 초석이 되고 있다. 또한 CSE int’l Luncheon을 개최하여 " +
            "학부 내 외국인 구성원의 화합과 생활의 불편함을 최소화하는 등 학부 차원에서 최선을 다하고 있다."
        val professors =
            professorRepository.findByLanguageAndStatusNot(
                enumLanguageType,
                ProfessorStatus.INACTIVE
            ).map {
                val imageURL = mainImageService.createImageURL(it.mainImage)
                SimpleProfessorDto.of(it, imageURL)
            }
                .sortedWith { a, b ->
                    when {
                        startsWithEnglish(a.name) && !startsWithEnglish(b.name) -> 1
                        !startsWithEnglish(a.name) && !startsWithEnglish(b.name) -> -1
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
        }
            .sortedWith { a, b ->
                when {
                    startsWithEnglish(a.name) && !startsWithEnglish(b.name) -> 1
                    !startsWithEnglish(a.name) && !startsWithEnglish(b.name) -> -1
                    else -> a.name.compareTo(b.name)
                }
            }
    }

    override fun updateProfessor(
        professorId: Long,
        updateProfessorRequest: ProfessorDto,
        mainImage: MultipartFile?
    ): ProfessorDto {
        val professor = professorRepository.findByIdOrNull(professorId)
            ?: throw CserealException.Csereal404("해당 교수님을 찾을 수 없습니다. professorId: $professorId")

        val outdatedLabId = professor.lab?.id

        if (updateProfessorRequest.labId != null && updateProfessorRequest.labId != professor.lab?.id) {
            val lab = labRepository.findByIdOrNull(updateProfessorRequest.labId)
                ?: throw CserealException.Csereal404(
                    "해당 연구실을 찾을 수 없습니다. LabId: ${updateProfessorRequest.labId}"
                )
            professor.addLab(lab)
        }

        professor.update(updateProfessorRequest)

        if (mainImage != null) {
            mainImageService.uploadMainImage(professor, mainImage)
        } else {
            professor.mainImage = null
        }

        // 학력 업데이트
        val oldEducations = professor.educations.map { it.name }

        val educationsToRemove = oldEducations - updateProfessorRequest.educations
        val educationsToAdd = updateProfessorRequest.educations - oldEducations

        professor.educations.removeIf { it.name in educationsToRemove }

        for (education in educationsToAdd) {
            EducationEntity.create(education, professor)
        }

        // 연구 분야 업데이트
        val oldResearchAreas = professor.researchAreas.map { it.name }

        val researchAreasToRemove = oldResearchAreas - updateProfessorRequest.researchAreas
        val researchAreasToAdd = updateProfessorRequest.researchAreas - oldResearchAreas

        professor.researchAreas.removeIf { it.name in researchAreasToRemove }

        for (researchArea in researchAreasToAdd) {
            ResearchAreaEntity.create(researchArea, professor)
        }

        // 경력 업데이트
        val oldCareers = professor.careers.map { it.name }

        val careersToRemove = oldCareers - updateProfessorRequest.careers
        val careersToAdd = updateProfessorRequest.careers - oldCareers

        professor.careers.removeIf { it.name in careersToRemove }

        for (career in careersToAdd) {
            CareerEntity.create(career, professor)
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

    override fun deleteProfessor(professorId: Long) {
        val professorEntity = professorRepository.findByIdOrNull(professorId) ?: return

        professorRepository.deleteById(professorId)

        applicationEventPublisher.publishEvent(
            ProfessorDeletedEvent.of(professorEntity)
        )
    }

    @Transactional
    override fun migrateProfessors(requestList: List<ProfessorDto>): List<ProfessorDto> {
        val list = mutableListOf<ProfessorDto>()

        for (request in requestList) {
            val enumLanguageType = LanguageType.makeStringToLanguageType(request.language)
            val professor = ProfessorEntity.of(enumLanguageType, request)
            if (request.labName != null) {
                val lab = labRepository.findByName(request.labName)
                    ?: throw CserealException.Csereal404(
                        "해당 연구실을 찾을 수 없습니다. LabName: ${request.labName}"
                    )
                professor.addLab(lab)
            }

            for (education in request.educations) {
                EducationEntity.create(education, professor)
            }

            for (researchArea in request.researchAreas) {
                ResearchAreaEntity.create(researchArea, professor)
            }

            for (career in request.careers) {
                CareerEntity.create(career, professor)
            }

            professor.memberSearch = MemberSearchEntity.create(professor)

            professorRepository.save(professor)

            list.add(ProfessorDto.of(professor, null))
        }
        return list
    }

    @Transactional
    override fun migrateProfessorImage(professorId: Long, mainImage: MultipartFile): ProfessorDto {
        val professor = professorRepository.findByIdOrNull(professorId)
            ?: throw CserealException.Csereal404("해당 교수님을 찾을 수 없습니다. professorId: $professorId")

        mainImageService.uploadMainImage(professor, mainImage)

        val imageURL = mainImageService.createImageURL(professor.mainImage)

        return ProfessorDto.of(professor, imageURL)
    }
}
