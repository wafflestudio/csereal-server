package com.wafflestudio.csereal.core.academics.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.enums.LanguageType
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
    fun createAcademics(
        studentType: String,
        postType: String,
        request: AcademicsDto,
        attachments: List<MultipartFile>?
    ): AcademicsDto

    fun readGuide(language: String, studentType: String): GuidePageResponse
    fun readAcademicsYearResponses(
        language: String,
        studentType: String,
        postType: String
    ): List<AcademicsYearResponse>
    fun readGeneralStudiesRequirements(language: String): GeneralStudiesRequirementsPageResponse
    fun readDegreeRequirements(language: String): DegreeRequirementsPageResponse
    fun createCourse(
        studentType: String,
        request: CourseDto,
        attachments: List<MultipartFile>?
    ): CourseDto
    fun readAllCourses(language: String, studentType: String): List<CourseDto>
    fun readCourse(language: String, name: String): CourseDto
    fun createScholarshipDetail(
        studentType: String,
        request: ScholarshipDto
    ): ScholarshipDto
    fun readAllScholarship(language: String, studentType: String): ScholarshipPageResponse
    fun readScholarship(scholarshipId: Long): ScholarshipDto
    fun migrateAcademicsDetail(
        studentType: String,
        postType: String,
        requestList: List<AcademicsDto>
    ): List<AcademicsDto>
    fun migrateCourses(studentType: String, requestList: List<CourseDto>): List<CourseDto>
    fun migrateScholarshipDetail(
        studentType: String,
        requestList: List<ScholarshipDto>
    ): List<ScholarshipDto>
    fun migrateAcademicsDetailAttachments(
        academicsId: Long,
        attachments: List<MultipartFile>?
    ): AcademicsDto
}

// TODO: add Update, Delete method
//       remember to update academicsSearch Field on Update method
//       remember to mark delete of academicsSearch Field on Delete mark method

@Service
class AcademicsServiceImpl(
    private val academicsRepository: AcademicsRepository,
    private val courseRepository: CourseRepository,
    private val attachmentService: AttachmentService,
    private val scholarshipRepository: ScholarshipRepository
) : AcademicsService {
    @Transactional
    override fun createAcademics(
        studentType: String,
        postType: String,
        request: AcademicsDto,
        attachments: List<MultipartFile>?
    ): AcademicsDto {
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val enumPostType = makeStringToAcademicsPostType(postType)
        val enumLanguageType = LanguageType.makeStringToLanguageType(request.language)
        val newAcademics =
            AcademicsEntity.of(enumStudentType, enumPostType, enumLanguageType, request)

        if (attachments != null) {
            attachmentService.uploadAllAttachments(newAcademics, attachments)
        }

        // create search data
        newAcademics.apply {
            academicsSearch = AcademicsSearchEntity.create(this)
        }

        academicsRepository.save(newAcademics)

        val attachmentResponses =
            attachmentService.createAttachmentResponses(newAcademics.attachments)

        return AcademicsDto.of(newAcademics, attachmentResponses)
    }

    @Transactional(readOnly = true)
    override fun readGuide(language: String, studentType: String): GuidePageResponse {
        val languageType = LanguageType.makeStringToLanguageType(language)
        val enumStudentType = makeStringToAcademicsStudentType(studentType)

        val academicsEntity =
            academicsRepository.findByLanguageAndStudentTypeAndPostType(
                languageType,
                enumStudentType,
                AcademicsPostType.GUIDE
            )
        val attachmentResponses =
            attachmentService.createAttachmentResponses(academicsEntity.attachments)
        return GuidePageResponse.of(academicsEntity, attachmentResponses)
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
    override fun readGeneralStudiesRequirements(language: String): GeneralStudiesRequirementsPageResponse {
        val enumLanguageType = LanguageType.makeStringToLanguageType(language)
        val overview =
            academicsRepository.findByLanguageAndStudentTypeAndPostTypeAndYear(
                enumLanguageType,
                AcademicsStudentType.UNDERGRADUATE,
                AcademicsPostType.GENERAL_STUDIES_REQUIREMENTS,
                null
            )
        val subjectChanges =
            academicsRepository.findByLanguageAndStudentTypeAndPostType(
                enumLanguageType,
                AcademicsStudentType.UNDERGRADUATE,
                AcademicsPostType.GENERAL_STUDIES_REQUIREMENTS_SUBJECT_CHANGES
            )
        val generalStudiesEntity =
            academicsRepository.findAllByLanguageAndStudentTypeAndPostTypeOrderByYearDesc(
                enumLanguageType,
                AcademicsStudentType.UNDERGRADUATE,
                AcademicsPostType.GENERAL_STUDIES_REQUIREMENTS
            ).filter { academicsEntity -> academicsEntity.year != null }
        return GeneralStudiesRequirementsPageResponse.of(overview, subjectChanges, generalStudiesEntity)
    }

    @Transactional(readOnly = true)
    override fun readDegreeRequirements(language: String): DegreeRequirementsPageResponse {
        val enumLanguageType = LanguageType.makeStringToLanguageType(language)
        val academicsEntity =
            academicsRepository.findByLanguageAndStudentTypeAndPostType(
                enumLanguageType,
                AcademicsStudentType.UNDERGRADUATE,
                AcademicsPostType.DEGREE_REQUIREMENTS
            )

        val yearList =
            academicsRepository.findAllByLanguageAndStudentTypeAndPostTypeOrderByYearDesc(
                enumLanguageType,
                AcademicsStudentType.UNDERGRADUATE,
                AcademicsPostType.DEGREE_REQUIREMENTS_YEAR_LIST
            ).map {
                val attachments = attachmentService.createAttachmentResponses(it.attachments)
                DegreeRequirementsDto.of(it, attachments)
            }

        return DegreeRequirementsPageResponse.of(academicsEntity, yearList)
    }

    @Transactional
    override fun createCourse(
        studentType: String,
        request: CourseDto,
        attachments: List<MultipartFile>?
    ): CourseDto {
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val enumLanguageType = LanguageType.makeStringToLanguageType(request.language)

        val newCourse = CourseEntity.of(enumStudentType, enumLanguageType, request)

        if (attachments != null) {
            attachmentService.uploadAllAttachments(newCourse, attachments)
        }

        // create search data
        newCourse.apply {
            academicsSearch = AcademicsSearchEntity.create(this)
        }
        courseRepository.save(newCourse)

        val attachmentResponses =
            attachmentService.createAttachmentResponses(newCourse.attachments)

        return CourseDto.of(newCourse, attachmentResponses)
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
                val attachmentResponses =
                    attachmentService.createAttachmentResponses(it.attachments)

                CourseDto.of(it, attachmentResponses)
            }
        return courseDtoList
    }

    @Transactional(readOnly = true)
    override fun readCourse(language: String, name: String): CourseDto {
        val enumLanguageType = LanguageType.makeStringToLanguageType(language)
        val course = courseRepository.findByLanguageAndName(enumLanguageType, name)
        val attachmentResponses = attachmentService.createAttachmentResponses(course.attachments)

        return CourseDto.of(course, attachmentResponses)
    }

    @Transactional
    override fun createScholarshipDetail(studentType: String, request: ScholarshipDto): ScholarshipDto {
        val enumLanguageType = LanguageType.makeStringToLanguageType(request.language)
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        var newScholarship = ScholarshipEntity.of(enumLanguageType, enumStudentType, request)

        // create search data
        newScholarship.apply {
            academicsSearch = AcademicsSearchEntity.create(this)
        }

        newScholarship = scholarshipRepository.save(newScholarship)

        return ScholarshipDto.of(newScholarship)
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
            )
        val scholarshipEntityList = scholarshipRepository.findAllByStudentType(enumStudentType)

        return ScholarshipPageResponse.of(academicsEntity, scholarshipEntityList)
    }

    @Transactional(readOnly = true)
    override fun readScholarship(scholarshipId: Long): ScholarshipDto {
        val scholarship = scholarshipRepository.findByIdOrNull(scholarshipId)
            ?: throw CserealException.Csereal404("해당하는 장학제도를 찾을 수 없습니다")
        return ScholarshipDto.of(scholarship)
    }

    @Transactional
    override fun migrateAcademicsDetail(
        studentType: String,
        postType: String,
        requestList: List<AcademicsDto>
    ): List<AcademicsDto> {
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val enumPostType = makeStringToAcademicsPostType(postType)
        val list = mutableListOf<AcademicsDto>()
        for (request in requestList) {
            val enumLanguageType = LanguageType.makeStringToLanguageType(request.language)
            val newAcademics = AcademicsEntity.of(
                enumStudentType,
                enumPostType,
                enumLanguageType,
                request
            )

            newAcademics.apply {
                academicsSearch = AcademicsSearchEntity.create(this)
            }

            academicsRepository.save(newAcademics)

            list.add(AcademicsDto.of(newAcademics, listOf()))
        }

        return list
    }

    @Transactional
    override fun migrateCourses(
        studentType: String,
        requestList: List<CourseDto>
    ): List<CourseDto> {
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val list = mutableListOf<CourseDto>()
        for (request in requestList) {
            val enumLanguageType = LanguageType.makeStringToLanguageType(request.language)
            val newCourse = CourseEntity.of(enumStudentType, enumLanguageType, request)

            newCourse.apply {
                academicsSearch = AcademicsSearchEntity.create(this)
            }
            courseRepository.save(newCourse)

            list.add(CourseDto.of(newCourse, listOf()))
        }

        return list
    }

    @Transactional
    override fun migrateScholarshipDetail(
        studentType: String,
        requestList: List<ScholarshipDto>
    ): List<ScholarshipDto> {
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val list = mutableListOf<ScholarshipDto>()
        for (request in requestList) {
            val enumLanguageType = LanguageType.makeStringToLanguageType(request.language)
            val newScholarship = ScholarshipEntity.of(
                enumLanguageType,
                enumStudentType,
                request
            )

            newScholarship.apply {
                academicsSearch = AcademicsSearchEntity.create(this)
            }

            scholarshipRepository.save(newScholarship)

            list.add(ScholarshipDto.of(newScholarship))
        }

        return list
    }

    @Transactional
    override fun migrateAcademicsDetailAttachments(
        academicsId: Long,
        attachments: List<MultipartFile>?
    ): AcademicsDto {
        val academics = academicsRepository.findByIdOrNull(academicsId)
            ?: throw CserealException.Csereal404("해당 내용을 찾을 수 없습니다.")

        if (attachments != null) {
            attachmentService.uploadAllAttachments(academics, attachments)
        }

        val attachmentResponses = attachmentService.createAttachmentResponses(
            academics.attachments
        )

        return AcademicsDto.of(academics, attachmentResponses)
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
