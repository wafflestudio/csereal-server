package com.wafflestudio.csereal.core.academics.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.repository.LanguageRepository
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

    fun readGuide(studentType: String): GuidePageResponse
    fun readAcademicsYearResponses(studentType: String, postType: String): List<AcademicsYearResponse>
    fun readGeneralStudies(): GeneralStudiesPageResponse
    fun createCourse(studentType: String, request: CourseDto, attachments: List<MultipartFile>?): CourseDto
    fun readAllCourses(studentType: String): List<CourseDto>
    fun readCourse(name: String): CourseDto
    fun createScholarshipDetail(studentType: String, request: ScholarshipDto): ScholarshipDto
    fun readAllScholarship(studentType: String): ScholarshipPageResponse
    fun readScholarship(scholarshipId: Long): ScholarshipDto
}

@Service
class AcademicsServiceImpl(
    private val academicsRepository: AcademicsRepository,
    private val courseRepository: CourseRepository,
    private val attachmentService: AttachmentService,
    private val scholarshipRepository: ScholarshipRepository,
    private val languageRepository: LanguageRepository
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
        val enumLanguageType = languageRepository.makeStringToLanguageType(request.language)
        val newAcademics = AcademicsEntity.of(enumStudentType, enumPostType, enumLanguageType, request)

        if (attachments != null) {
            attachmentService.uploadAllAttachments(newAcademics, attachments)
        }

        academicsRepository.save(newAcademics)

        val attachmentResponses = attachmentService.createAttachmentResponses(newAcademics.attachments)

        return AcademicsDto.of(newAcademics, attachmentResponses)
    }

    @Transactional(readOnly = true)
    override fun readGuide(studentType: String): GuidePageResponse {
        val enumStudentType = makeStringToAcademicsStudentType(studentType)

        val academicsEntity = academicsRepository.findByStudentTypeAndPostType(enumStudentType, AcademicsPostType.GUIDE)
        val attachmentResponses = attachmentService.createAttachmentResponses(academicsEntity.attachments)
        return GuidePageResponse.of(academicsEntity, attachmentResponses)
    }

    @Transactional(readOnly = true)
    override fun readAcademicsYearResponses(studentType: String, postType: String): List<AcademicsYearResponse> {
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val enumPostType = makeStringToAcademicsPostType(postType)

        val academicsEntityList = academicsRepository.findAllByStudentTypeAndPostTypeOrderByYearDesc(
            enumStudentType,
            enumPostType
        )

        val academicsYearResponses = academicsEntityList.map {
            val attachments = attachmentService.createAttachmentResponses(it.attachments)
            AcademicsYearResponse.of(it, attachments)
        }

        return academicsYearResponses
    }

    override fun readGeneralStudies(): GeneralStudiesPageResponse {
        val academicsEntity = academicsRepository.findByStudentTypeAndPostType(
            AcademicsStudentType.UNDERGRADUATE,
            AcademicsPostType.GENERAL_STUDIES_REQUIREMENTS
        )
        val subjectChangesList = academicsRepository.findAllByStudentTypeAndPostTypeOrderByTimeDesc(
            AcademicsStudentType.UNDERGRADUATE,
            AcademicsPostType.GENERAL_STUDIES_REQUIREMENTS_SUBJECT_CHANGES
        )

        return GeneralStudiesPageResponse.of(academicsEntity, subjectChangesList)
    }

    @Transactional
    override fun createCourse(studentType: String, request: CourseDto, attachments: List<MultipartFile>?): CourseDto {
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val enumLanguageType = languageRepository.makeStringToLanguageType(request.language)

        val newCourse = CourseEntity.of(enumStudentType, enumLanguageType, request)

        if (attachments != null) {
            attachmentService.uploadAllAttachments(newCourse, attachments)
        }

        courseRepository.save(newCourse)

        val attachmentResponses = attachmentService.createAttachmentResponses(newCourse.attachments)

        return CourseDto.of(newCourse, attachmentResponses)
    }

    @Transactional(readOnly = true)
    override fun readAllCourses(studentType: String): List<CourseDto> {
        val enumStudentType = makeStringToAcademicsStudentType(studentType)

        val courseDtoList = courseRepository.findAllByStudentTypeOrderByNameAsc(enumStudentType).map {
            val attachmentResponses = attachmentService.createAttachmentResponses(it.attachments)
            CourseDto.of(it, attachmentResponses)
        }
        return courseDtoList
    }

    @Transactional(readOnly = true)
    override fun readCourse(name: String): CourseDto {
        val course = courseRepository.findByName(name)
        val attachmentResponses = attachmentService.createAttachmentResponses(course.attachments)

        return CourseDto.of(course, attachmentResponses)
    }

    @Transactional
    override fun createScholarshipDetail(studentType: String, request: ScholarshipDto): ScholarshipDto {
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val newScholarship = ScholarshipEntity.of(enumStudentType, request)

        scholarshipRepository.save(newScholarship)

        return ScholarshipDto.of(newScholarship)
    }

    @Transactional(readOnly = true)
    override fun readAllScholarship(studentType: String): ScholarshipPageResponse {
        val enumStudentType = makeStringToAcademicsStudentType(studentType)

        val academicsEntity = academicsRepository.findByStudentTypeAndPostType(
            enumStudentType,
            AcademicsPostType.SCHOLARSHIP
        )
        val scholarshipEntityList = scholarshipRepository.findAllByStudentType(enumStudentType)

        return ScholarshipPageResponse.of(academicsEntity, scholarshipEntityList)
    }

    @Transactional(readOnly = true)
    override fun readScholarship(scholarshipId: Long): ScholarshipDto {
        val scholarship = scholarshipRepository.findByIdOrNull(scholarshipId)
            ?: throw CserealException.Csereal404("id: $scholarshipId 에 해당하는 장학제도를 찾을 수 없습니다")
        return ScholarshipDto.of(scholarship)
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
