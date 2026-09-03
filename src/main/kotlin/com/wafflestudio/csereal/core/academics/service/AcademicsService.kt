package com.wafflestudio.csereal.core.academics.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.academics.api.req.*
import com.wafflestudio.csereal.core.academics.database.*
import com.wafflestudio.csereal.core.academics.dto.*
import com.wafflestudio.csereal.core.resource.attachment.service.AttachmentService
import com.wafflestudio.csereal.core.academics.database.ScholarshipRepository
import com.wafflestudio.csereal.core.academics.dto.ScholarshipDto
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface AcademicsService {
    fun readGuide(language: LanguageType, studentType: AcademicsStudentType): GuidePageResponse
    fun readAcademicsYearResponses(
        language: LanguageType,
        studentType: AcademicsStudentType,
        postType: AcademicsPostType
    ): List<AcademicsYearResponse>

    fun readDegreeRequirements(language: LanguageType): DegreeRequirementsPageResponse
    fun updateDegreeRequirements(language: LanguageType, request: UpdateSingleReq, newAttachments: List<MultipartFile>?)
    fun createCourse(request: GroupedCourseDto)

    fun readAllGroupedCourses(studentType: AcademicsStudentType, sortType: String): List<GroupedCourseDto>
    fun updateCourse(updateRequest: GroupedCourseDto)
    fun deleteCourse(code: String)
    fun updateScholarshipPage(
        language: LanguageType,
        studentType: AcademicsStudentType,
        request: UpdateScholarshipPageReq
    )

    fun readAllScholarship(language: LanguageType, studentType: AcademicsStudentType): ScholarshipPageResponse
    fun createScholarship(
        studentType: AcademicsStudentType,
        request: CreateScholarshipReq
    )

    fun readScholarshipV2(scholarshipId: Long): Pair<ScholarshipDto, ScholarshipDto>
    fun updateScholarship(request: UpdateScholarshipReq)
    fun deleteScholarship(scholarshipId: Long)
    fun updateGuide(
        language: LanguageType,
        studentType: AcademicsStudentType,
        request: UpdateSingleReq,
        newAttachments: List<MultipartFile>?
    )

    fun updateAcademicsYearResponse(
        language: LanguageType,
        studentType: AcademicsStudentType,
        postType: AcademicsPostType,
        year: Int,
        request: UpdateYearReq,
        newAttachments: List<MultipartFile>?
    )

    fun deleteAcademicsYearResponse(
        language: LanguageType,
        studentType: AcademicsStudentType,
        postType: AcademicsPostType,
        year: Int
    )
    fun createAcademicsYearResponse(
        language: LanguageType,
        studentType: AcademicsStudentType,
        postType: AcademicsPostType,
        request: CreateYearReq,
        attachments: List<MultipartFile>?
    )
}

// TODO: add Update, Delete method
//       remember to update academicsSearch Field on Update method
//       remember to mark delete of academicsSearch Field on Delete mark method

@Service
class AcademicsServiceImpl(
    private val academicsRepository: AcademicsRepository,
    private val courseRepository: CourseRepository,
    private val attachmentService: AttachmentService,
    private val scholarshipRepository: ScholarshipRepository,
    private val scholarshipLanguageRepository: ScholarshipLanguageRepository
) : AcademicsService {

    @Transactional(readOnly = true)
    override fun readGuide(language: LanguageType, studentType: AcademicsStudentType): GuidePageResponse {
        val academicsEntity =
            academicsRepository.findByLanguageAndStudentTypeAndPostType(
                language,
                studentType,
                AcademicsPostType.GUIDE
            ) ?: throw CserealException.Csereal404("Guide Not Found")
        val attachmentResponses =
            attachmentService.createAttachmentResponses(academicsEntity.attachments)
        return GuidePageResponse.of(academicsEntity, attachmentResponses)
    }

    @Transactional
    override fun updateGuide(
        language: LanguageType,
        studentType: AcademicsStudentType,
        request: UpdateSingleReq,
        newAttachments: List<MultipartFile>?
    ) {
        val academicsEntity =
            academicsRepository.findByLanguageAndStudentTypeAndPostType(
                language,
                studentType,
                AcademicsPostType.GUIDE
            ) ?: throw CserealException.Csereal404("Guide Not Found")

        academicsEntity.description = request.description
        academicsEntity.academicsSearch?.update(academicsEntity) ?: let {
            academicsEntity.academicsSearch = AcademicsSearchEntity.create(academicsEntity)
        }

        attachmentService.syncAttachments(academicsEntity, request.attachmentIds, newAttachments)
    }

    @Transactional
    override fun updateAcademicsYearResponse(
        language: LanguageType,
        studentType: AcademicsStudentType,
        postType: AcademicsPostType,
        year: Int,
        request: UpdateYearReq,
        newAttachments: List<MultipartFile>?
    ) {
        val academicsEntity = academicsRepository.findByLanguageAndStudentTypeAndPostTypeAndYear(
            language,
            studentType,
            postType,
            year
        ) ?: throw CserealException.Csereal404("AcademicsEntity Not Found")

        academicsEntity.description = request.description
        academicsEntity.academicsSearch?.update(academicsEntity) ?: let {
            academicsEntity.academicsSearch = AcademicsSearchEntity.create(academicsEntity)
        }

        attachmentService.syncAttachments(academicsEntity, request.attachmentIds, newAttachments)
    }

    @Transactional
    override fun deleteAcademicsYearResponse(
        language: LanguageType,
        studentType: AcademicsStudentType,
        postType: AcademicsPostType,
        year: Int
    ) {
        val academicsEntity = academicsRepository.findByLanguageAndStudentTypeAndPostTypeAndYear(
            language,
            studentType,
            postType,
            year
        ) ?: throw CserealException.Csereal404("AcademicsEntity Not Found")

        attachmentService.deleteAttachments(academicsEntity.attachments.map { it.id })
        academicsRepository.delete(academicsEntity)
    }

    @Transactional
    override fun createAcademicsYearResponse(
        language: LanguageType,
        studentType: AcademicsStudentType,
        postType: AcademicsPostType,
        request: CreateYearReq,
        attachments: List<MultipartFile>?
    ) {
        academicsRepository.findByLanguageAndStudentTypeAndPostTypeAndYear(
            language,
            studentType,
            postType,
            request.year
        )?.let {
            throw CserealException.Csereal409("Year Response Already Exist")
        }

        val newAcademics =
            AcademicsEntity.createYearResponse(studentType, postType, language, request)

        newAcademics.apply {
            academicsSearch = AcademicsSearchEntity.create(this)
        }

        if (attachments != null) {
            attachmentService.uploadAllAttachments(newAcademics, attachments)
        }

        academicsRepository.save(newAcademics)
    }

    @Transactional(readOnly = true)
    override fun readAcademicsYearResponses(
        language: LanguageType,
        studentType: AcademicsStudentType,
        postType: AcademicsPostType
    ): List<AcademicsYearResponse> {
        val academicsEntityList =
            academicsRepository.findAllByLanguageAndStudentTypeAndPostTypeOrderByYearDesc(
                language,
                studentType,
                postType
            )

        val academicsYearResponses = academicsEntityList.map {
            val attachments = attachmentService.createAttachmentResponses(it.attachments)
            AcademicsYearResponse.of(it, attachments)
        }

        return academicsYearResponses
    }

    @Transactional(readOnly = true)
    override fun readDegreeRequirements(language: LanguageType): DegreeRequirementsPageResponse {
        val academicsEntity =
            academicsRepository.findByLanguageAndStudentTypeAndPostType(
                language,
                AcademicsStudentType.UNDERGRADUATE,
                AcademicsPostType.DEGREE_REQUIREMENTS
            ) ?: throw CserealException.Csereal404("Degree Requirements Not Found")

        val attachments = attachmentService.createAttachmentResponses(academicsEntity.attachments)
        return DegreeRequirementsPageResponse.of(academicsEntity, attachments)
    }

    @Transactional
    override fun updateDegreeRequirements(
        language: LanguageType,
        request: UpdateSingleReq,
        newAttachments: List<MultipartFile>?
    ) {
        val academicsEntity =
            academicsRepository.findByLanguageAndStudentTypeAndPostType(
                language,
                AcademicsStudentType.UNDERGRADUATE,
                AcademicsPostType.DEGREE_REQUIREMENTS
            ) ?: throw CserealException.Csereal404("Degree Requirements Not Found")

        academicsEntity.description = request.description
        academicsEntity.academicsSearch?.update(academicsEntity) ?: let {
            academicsEntity.academicsSearch = AcademicsSearchEntity.create(academicsEntity)
        }

        attachmentService.syncAttachments(academicsEntity, request.attachmentIds, newAttachments)
    }

    @Transactional
    override fun createCourse(request: GroupedCourseDto) {
        if (courseRepository.existsByCode(request.code)) {
            throw CserealException.Csereal409("해당 교과목 번호를 가지고 있는 엔티티가 이미 있습니다")
        }

        val studentType = makeStringToAcademicsStudentType(request.studentType)

        val courses = listOf(
            LanguageType.KO to request.ko,
            LanguageType.EN to request.en
        ).map { (language, langSpecificData) ->
            CourseEntity.of(
                studentType,
                language,
                langSpecificData.classification,
                request.code,
                langSpecificData.name,
                request.credit,
                request.grade,
                langSpecificData.description
            ).apply {
                academicsSearch = AcademicsSearchEntity.create(this)
            }
        }

        courseRepository.saveAll(courses)
    }

    @Transactional(readOnly = true)
    override fun readAllGroupedCourses(studentType: AcademicsStudentType, sortType: String): List<GroupedCourseDto> {
        val sort = LanguageType.makeStringToLanguageType(sortType)
        return courseRepository.findGroupedCourses(studentType)
            .map(CourseMapper::toGroupedCourseDTO)
            .sortedBy { course ->
                when (sort) {
                    LanguageType.KO -> course.ko.name
                    LanguageType.EN -> course.en.name
                }
            }
    }

    @Transactional
    override fun updateCourse(updateRequest: GroupedCourseDto) {
        val ko = courseRepository.findByCodeAndLanguage(updateRequest.code, LanguageType.KO)
            ?: throw CserealException.Csereal404("korean course not found")
        val en = courseRepository.findByCodeAndLanguage(updateRequest.code, LanguageType.EN)
            ?: throw CserealException.Csereal404("english course not found")

        listOf(ko, en).forEach { course ->
            course.apply {
                credit = updateRequest.credit
                grade = updateRequest.grade
                studentType = makeStringToAcademicsStudentType(updateRequest.studentType)
                val langSpecificData = if (language == LanguageType.KO) updateRequest.ko else updateRequest.en
                name = langSpecificData.name
                description = langSpecificData.description
                classification = langSpecificData.classification
            }
            course.academicsSearch?.update(course) ?: let {
                course.academicsSearch = AcademicsSearchEntity.create(course)
            }
        }
    }

    @Transactional
    override fun deleteCourse(code: String) {
        if (!courseRepository.existsByCode(code)) {
            throw CserealException.Csereal404("entity not found")
        }
        courseRepository.deleteAllByCode(code)
    }

    @Transactional
    override fun updateScholarshipPage(
        language: LanguageType,
        studentType: AcademicsStudentType,
        request: UpdateScholarshipPageReq
    ) {
        val scholarshipPage = academicsRepository.findByLanguageAndStudentTypeAndPostType(
            language,
            studentType,
            AcademicsPostType.SCHOLARSHIP
        ) ?: throw CserealException.Csereal404("scholarship page not found")

        scholarshipPage.description = request.description
        scholarshipPage.academicsSearch?.update(scholarshipPage) ?: let {
            scholarshipPage.academicsSearch = AcademicsSearchEntity.create(scholarshipPage)
        }
    }

    @Transactional(readOnly = true)
    override fun readAllScholarship(
        language: LanguageType,
        studentType: AcademicsStudentType
    ): ScholarshipPageResponse {
        val academicsEntity =
            academicsRepository.findByLanguageAndStudentTypeAndPostType(
                language,
                studentType,
                AcademicsPostType.SCHOLARSHIP
            ) ?: throw CserealException.Csereal404("Scholarship Entity Not Found")
        val scholarshipEntityList =
            scholarshipRepository.findAllByStudentTypeAndLanguage(studentType, language)

        return ScholarshipPageResponse.of(academicsEntity, scholarshipEntityList)
    }

    @Transactional
    override fun createScholarship(studentType: AcademicsStudentType, request: CreateScholarshipReq) {
        val koScholarship =
            ScholarshipEntity.of(LanguageType.KO, studentType, request.koName, request.koDescription)
        val enScholarship =
            ScholarshipEntity.of(LanguageType.EN, studentType, request.enName, request.enDescription)

        // create search data
        koScholarship.apply {
            academicsSearch = AcademicsSearchEntity.create(this)
        }
        enScholarship.apply {
            academicsSearch = AcademicsSearchEntity.create(this)
        }

        scholarshipRepository.save(koScholarship)
        scholarshipRepository.save(enScholarship)
        scholarshipLanguageRepository.save(ScholarshipLanguageEntity(koScholarship, enScholarship))
    }

    @Transactional(readOnly = true)
    override fun readScholarshipV2(scholarshipId: Long): Pair<ScholarshipDto, ScholarshipDto> {
        val scholarship = scholarshipRepository.findByIdOrNull(scholarshipId)
            ?: throw CserealException.Csereal404("해당하는 장학제도를 찾을 수 없습니다")

        val correspondingScholarship = when (scholarship.language) {
            LanguageType.KO -> scholarshipLanguageRepository.findByKoScholarship(scholarship)!!.enScholarship
            LanguageType.EN -> scholarshipLanguageRepository.findByEnScholarship(scholarship)!!.koScholarship
        }

        return Pair(ScholarshipDto.of(scholarship), ScholarshipDto.of(correspondingScholarship))
    }

    @Transactional
    override fun updateScholarship(request: UpdateScholarshipReq) {
        val koScholarship = scholarshipRepository.findByIdOrNull(request.ko.id)
            ?: throw CserealException.Csereal404("해당하는 장학제도를 찾을 수 없습니다")
        val enScholarship = scholarshipRepository.findByIdOrNull(request.en.id)
            ?: throw CserealException.Csereal404("해당하는 장학제도를 찾을 수 없습니다")

        koScholarship.name = request.ko.name
        koScholarship.description = request.ko.description
        enScholarship.name = request.en.name
        enScholarship.description = request.en.description

        koScholarship.academicsSearch?.update(koScholarship) ?: let {
            koScholarship.academicsSearch = AcademicsSearchEntity.create(koScholarship)
        }
        enScholarship.academicsSearch?.update(enScholarship) ?: let {
            enScholarship.academicsSearch = AcademicsSearchEntity.create(enScholarship)
        }
    }

    @Transactional
    override fun deleteScholarship(scholarshipId: Long) {
        val scholarship = scholarshipRepository.findByIdOrNull(scholarshipId)
            ?: throw CserealException.Csereal404("해당하는 장학제도를 찾을 수 없습니다")

        val scholarshipLanguage = when (scholarship.language) {
            LanguageType.KO -> scholarshipLanguageRepository.findByKoScholarship(scholarship)
            LanguageType.EN -> scholarshipLanguageRepository.findByEnScholarship(scholarship)
        }

        scholarshipLanguageRepository.delete(scholarshipLanguage!!)
        scholarshipRepository.delete(scholarshipLanguage.koScholarship)
        scholarshipRepository.delete(scholarshipLanguage.enScholarship)
    }

    // JSON 바디의 studentType 필드용(문자열). URL 파라미터는 컨버터가 처리한다.
    private fun makeStringToAcademicsStudentType(value: String): AcademicsStudentType {
        try {
            return AcademicsStudentType.valueOf(value.replace("-", "_").uppercase())
        } catch (e: IllegalArgumentException) {
            throw CserealException.Csereal400("해당하는 enum을 찾을 수 없습니다")
        }
    }
}
