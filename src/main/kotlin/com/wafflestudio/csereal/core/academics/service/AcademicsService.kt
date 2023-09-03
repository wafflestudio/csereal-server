package com.wafflestudio.csereal.core.academics.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.about.database.AboutPostType
import com.wafflestudio.csereal.core.academics.database.*
import com.wafflestudio.csereal.core.academics.dto.CourseDto
import com.wafflestudio.csereal.core.academics.dto.AcademicsDto
import com.wafflestudio.csereal.core.resource.attachment.service.AttachmentService
import com.wafflestudio.csereal.core.academics.dto.ScholarshipPageResponse
import com.wafflestudio.csereal.core.scholarship.database.ScholarshipRepository
import com.wafflestudio.csereal.core.scholarship.dto.SimpleScholarshipDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

interface AcademicsService {
    fun createAcademics(studentType: String, postType: String, request: AcademicsDto, attachments: List<MultipartFile>?): AcademicsDto
    fun readAcademics(studentType: String, postType: String): AcademicsDto
    fun createCourse(studentType: String, request: CourseDto, attachments: List<MultipartFile>?): CourseDto
    fun readAllCourses(studentType: String): List<CourseDto>
    fun readCourse(name: String): CourseDto
    fun readScholarship(name: String): ScholarshipPageResponse
}

@Service
class AcademicsServiceImpl(
    private val academicsRepository: AcademicsRepository,
    private val courseRepository: CourseRepository,
    private val attachmentService: AttachmentService,
    private val scholarshipRepository: ScholarshipRepository
) : AcademicsService {
    @Transactional
    override fun createAcademics(studentType: String, postType: String, request: AcademicsDto, attachments: List<MultipartFile>?): AcademicsDto {
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val enumPostType = makeStringToAcademicsPostType(postType)

        val newAcademics = AcademicsEntity.of(enumStudentType, enumPostType, request)

        if(attachments != null) {
            attachmentService.uploadAllAttachments(newAcademics, attachments)
        }

        academicsRepository.save(newAcademics)

        val attachmentResponses = attachmentService.createAttachmentResponses(newAcademics.attachments)


        return AcademicsDto.of(newAcademics, attachmentResponses)
    }

    @Transactional(readOnly = true)
    override fun readAcademics(studentType: String, postType: String): AcademicsDto {

        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val enumPostType = makeStringToAcademicsPostType(postType)

        val academics = academicsRepository.findByStudentTypeAndPostType(enumStudentType, enumPostType)

        val attachmentResponses = attachmentService.createAttachmentResponses(academics.attachments)

        return AcademicsDto.of(academics, attachmentResponses)
    }

    @Transactional
    override fun createCourse(studentType: String, request: CourseDto, attachments: List<MultipartFile>?): CourseDto {
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val course = CourseEntity.of(enumStudentType, request)

        courseRepository.save(course)

        val attachmentResponses = attachmentService.createAttachmentResponses(course.attachments)

        return CourseDto.of(course, attachmentResponses)
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

    @Transactional(readOnly = true)
    override fun readScholarship(name: String): ScholarshipPageResponse {
        val scholarship = academicsRepository.findByName(name)
        val scholarships = scholarshipRepository.findAll()

        return ScholarshipPageResponse.of(scholarship, scholarships)
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
