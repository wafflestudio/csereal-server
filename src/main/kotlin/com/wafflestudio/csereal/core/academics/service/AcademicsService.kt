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
    fun readGuide(language: String, studentType: String): GuidePageResponse
    fun readAcademicsYearResponses(
        language: String,
        studentType: String,
        postType: String
    ): List<AcademicsYearResponse>

    fun readDegreeRequirements(language: String): DegreeRequirementsPageResponse
    fun updateDegreeRequirements(language: String, request: UpdateSingleReq, newAttachments: List<MultipartFile>?)
    fun createCourse(request: GroupedCourseDto)

    fun readAllCourses(language: String, studentType: String): List<CourseDto>
    fun readAllGroupedCourses(studentType: String, sortType: String): List<GroupedCourseDto>
    fun updateCourse(updateRequest: GroupedCourseDto)
    fun deleteCourse(code: String)
    fun updateScholarshipPage(language: String, studentType: String, request: UpdateScholarshipPageReq)

    fun readAllScholarship(language: String, studentType: String): ScholarshipPageResponse
    fun createScholarship(
        studentType: String,
        request: CreateScholarshipReq
    )

    fun readScholarship(scholarshipId: Long): ScholarshipDto
    fun readScholarshipV2(scholarshipId: Long): Pair<ScholarshipDto, ScholarshipDto>
    fun updateScholarship(request: UpdateScholarshipReq)
    fun deleteScholarship(scholarshipId: Long)
    fun updateGuide(
        language: String,
        studentType: String,
        request: UpdateSingleReq,
        newAttachments: List<MultipartFile>?
    )

    fun updateAcademicsYearResponse(
        language: String,
        studentType: String,
        postType: String,
        year: Int,
        request: UpdateYearReq
    )

    fun deleteAcademicsYearResponse(language: String, studentType: String, postType: String, year: Int)
    fun createAcademicsYearResponse(language: String, studentType: String, postType: String, request: CreateYearReq)
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
    override fun readGuide(language: String, studentType: String): GuidePageResponse {
        val languageType = LanguageType.makeStringToLanguageType(language)
        val enumStudentType = makeStringToAcademicsStudentType(studentType)

        val academicsEntity =
            academicsRepository.findByLanguageAndStudentTypeAndPostType(
                languageType,
                enumStudentType,
                AcademicsPostType.GUIDE
            ) ?: throw CserealException.Csereal404("Guide Not Found")
        val attachmentResponses =
            attachmentService.createAttachmentResponses(academicsEntity.attachments)
        return GuidePageResponse.of(academicsEntity, attachmentResponses)
    }

    @Transactional
    override fun updateGuide(
        language: String,
        studentType: String,
        request: UpdateSingleReq,
        newAttachments: List<MultipartFile>?
    ) {
        val languageType = LanguageType.makeStringToLanguageType(language)
        val enumStudentType = makeStringToAcademicsStudentType(studentType)

        val academicsEntity =
            academicsRepository.findByLanguageAndStudentTypeAndPostType(
                languageType,
                enumStudentType,
                AcademicsPostType.GUIDE
            ) ?: throw CserealException.Csereal404("Guide Not Found")

        academicsEntity.description = request.description
        academicsEntity.academicsSearch?.update(academicsEntity) ?: let {
            academicsEntity.academicsSearch = AcademicsSearchEntity.create(academicsEntity)
        }

        attachmentService.deleteAttachments(request.deleteIds)
        if (newAttachments != null) {
            attachmentService.uploadAllAttachments(academicsEntity, newAttachments)
        }
    }

    @Transactional
    override fun updateAcademicsYearResponse(
        language: String,
        studentType: String,
        postType: String,
        year: Int,
        request: UpdateYearReq
    ) {
        val languageType = LanguageType.makeStringToLanguageType(language)
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val enumPostType = makeStringToAcademicsPostType(postType)

        val academicsEntity = academicsRepository.findByLanguageAndStudentTypeAndPostTypeAndYear(
            languageType,
            enumStudentType,
            enumPostType,
            year
        ) ?: throw CserealException.Csereal404("AcademicsEntity Not Found")

        academicsEntity.description = request.description
        academicsEntity.academicsSearch?.update(academicsEntity) ?: let {
            academicsEntity.academicsSearch = AcademicsSearchEntity.create(academicsEntity)
        }
    }

    @Transactional
    override fun deleteAcademicsYearResponse(language: String, studentType: String, postType: String, year: Int) {
        val languageType = LanguageType.makeStringToLanguageType(language)
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val enumPostType = makeStringToAcademicsPostType(postType)

        val academicsEntity = academicsRepository.findByLanguageAndStudentTypeAndPostTypeAndYear(
            languageType,
            enumStudentType,
            enumPostType,
            year
        ) ?: throw CserealException.Csereal404("AcademicsEntity Not Found")

        academicsRepository.delete(academicsEntity)
    }

    @Transactional
    override fun createAcademicsYearResponse(
        language: String,
        studentType: String,
        postType: String,
        request: CreateYearReq
    ) {
        val languageType = LanguageType.makeStringToLanguageType(language)
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val enumPostType = makeStringToAcademicsPostType(postType)

        academicsRepository.findByLanguageAndStudentTypeAndPostTypeAndYear(
            languageType,
            enumStudentType,
            enumPostType,
            request.year
        )?.let {
            throw CserealException.Csereal409("Year Response Already Exist")
        }

        val newAcademics =
            AcademicsEntity.createYearResponse(enumStudentType, enumPostType, languageType, request)

        newAcademics.apply {
            academicsSearch = AcademicsSearchEntity.create(this)
        }

        academicsRepository.save(newAcademics)
    }

    @Transactional(readOnly = true)
    override fun readAcademicsYearResponses(
        language: String,
        studentType: String,
        postType: String
    ): List<AcademicsYearResponse> {
        val languageType = LanguageType.makeStringToLanguageType(language)
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val enumPostType = makeStringToAcademicsPostType(postType)

        val academicsEntityList =
            academicsRepository.findAllByLanguageAndStudentTypeAndPostTypeOrderByYearDesc(
                languageType,
                enumStudentType,
                enumPostType
            )

        val academicsYearResponses = academicsEntityList.map {
            val attachments = attachmentService.createAttachmentResponses(it.attachments)
            AcademicsYearResponse.of(it, attachments)
        }

        return academicsYearResponses
    }

    @Transactional(readOnly = true)
    override fun readDegreeRequirements(language: String): DegreeRequirementsPageResponse {
        val enumLanguageType = LanguageType.makeStringToLanguageType(language)

        val academicsEntity =
            academicsRepository.findByLanguageAndStudentTypeAndPostType(
                enumLanguageType,
                AcademicsStudentType.UNDERGRADUATE,
                AcademicsPostType.DEGREE_REQUIREMENTS
            ) ?: throw CserealException.Csereal404("Degree Requirements Not Found")

        val attachments = attachmentService.createAttachmentResponses(academicsEntity.attachments)
        return DegreeRequirementsPageResponse.of(academicsEntity, attachments)
    }

    @Transactional
    override fun updateDegreeRequirements(
        language: String,
        request: UpdateSingleReq,
        newAttachments: List<MultipartFile>?
    ) {
        val enumLanguageType = LanguageType.makeStringToLanguageType(language)

        val academicsEntity =
            academicsRepository.findByLanguageAndStudentTypeAndPostType(
                enumLanguageType,
                AcademicsStudentType.UNDERGRADUATE,
                AcademicsPostType.DEGREE_REQUIREMENTS
            ) ?: throw CserealException.Csereal404("Degree Requirements Not Found")

        academicsEntity.description = request.description
        academicsEntity.academicsSearch?.update(academicsEntity) ?: let {
            academicsEntity.academicsSearch = AcademicsSearchEntity.create(academicsEntity)
        }

        attachmentService.deleteAttachments(request.deleteIds)
        if (newAttachments != null) {
            attachmentService.uploadAllAttachments(academicsEntity, newAttachments)
        }
    }

    @Transactional
    override fun createCourse(request: GroupedCourseDto) {
        if (courseRepository.existsByCode(request.code)) {
            throw CserealException.Csereal409("해당 교과목 번호를 가지고 있는 엔티티가 이미 있습니다")
        }

        val enumStudentType = makeStringToAcademicsStudentType(request.studentType)

        val courses = listOf(
            LanguageType.KO to request.ko,
            LanguageType.EN to request.en
        ).map { (language, langSpecificData) ->
            CourseEntity.of(
                enumStudentType,
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
    override fun readAllCourses(language: String, studentType: String): List<CourseDto> {
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val enumLanguageType = LanguageType.makeStringToLanguageType(language)
        val courseDtoList =
            courseRepository.findAllByLanguageAndStudentTypeOrderByNameAsc(
                enumLanguageType,
                enumStudentType
            ).map {
                CourseDto.of(it)
            }
        return courseDtoList
    }

    @Transactional(readOnly = true)
    override fun readAllGroupedCourses(studentType: String, sortType: String): List<GroupedCourseDto> {
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val sort = LanguageType.makeStringToLanguageType(sortType)
        return courseRepository.findGroupedCourses(enumStudentType)
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
    override fun updateScholarshipPage(language: String, studentType: String, request: UpdateScholarshipPageReq) {
        val enumLanguageType = LanguageType.makeStringToLanguageType(language)
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val scholarshipPage = academicsRepository.findByLanguageAndStudentTypeAndPostType(
            enumLanguageType,
            enumStudentType,
            AcademicsPostType.SCHOLARSHIP
        ) ?: throw CserealException.Csereal404("scholarship page not found")

        scholarshipPage.description = request.description
        scholarshipPage.academicsSearch?.update(scholarshipPage) ?: let {
            scholarshipPage.academicsSearch = AcademicsSearchEntity.create(scholarshipPage)
        }
    }

    @Transactional(readOnly = true)
    override fun readAllScholarship(language: String, studentType: String): ScholarshipPageResponse {
        val enumLanguageType = LanguageType.makeStringToLanguageType(language)
        val enumStudentType = makeStringToAcademicsStudentType(studentType)

        val academicsEntity =
            academicsRepository.findByLanguageAndStudentTypeAndPostType(
                enumLanguageType,
                enumStudentType,
                AcademicsPostType.SCHOLARSHIP
            ) ?: throw CserealException.Csereal404("Scholarship Entity Not Found")
        val scholarshipEntityList =
            scholarshipRepository.findAllByStudentTypeAndLanguage(enumStudentType, enumLanguageType)

        return ScholarshipPageResponse.of(academicsEntity, scholarshipEntityList)
    }

    @Transactional
    override fun createScholarship(studentType: String, request: CreateScholarshipReq) {
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val koScholarship =
            ScholarshipEntity.of(LanguageType.KO, enumStudentType, request.koName, request.koDescription)
        val enScholarship =
            ScholarshipEntity.of(LanguageType.EN, enumStudentType, request.enName, request.enDescription)

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
    override fun readScholarship(scholarshipId: Long): ScholarshipDto {
        val scholarship = scholarshipRepository.findByIdOrNull(scholarshipId)
            ?: throw CserealException.Csereal404("해당하는 장학제도를 찾을 수 없습니다")
        return ScholarshipDto.of(scholarship)
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

    private fun makeStringToAcademicsStudentType(postType: String): AcademicsStudentType {
        try {
            val upperPostType = postType.replace("-", "_").uppercase()
            return AcademicsStudentType.valueOf(upperPostType)
        } catch (e: IllegalArgumentException) {
            throw CserealException.Csereal400("해당하는 enum을 찾을 수 없습니다")
        }
    }

    private fun makeStringToAcademicsPostType(postType: String): AcademicsPostType {
        try {
            val upperPostType = postType.replace("-", "_").uppercase()
            return AcademicsPostType.valueOf(upperPostType)
        } catch (e: IllegalArgumentException) {
            throw CserealException.Csereal400("해당하는 enum을 찾을 수 없습니다")
        }
    }
}
