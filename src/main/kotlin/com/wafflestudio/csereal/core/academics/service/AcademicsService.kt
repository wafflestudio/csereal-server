package com.wafflestudio.csereal.core.academics.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.about.database.AboutPostType
import com.wafflestudio.csereal.core.academics.database.*
import com.wafflestudio.csereal.core.academics.dto.CourseDto
import com.wafflestudio.csereal.core.academics.dto.AcademicsDto
import com.wafflestudio.csereal.core.academics.dto.ScholarshipPageResponse
import com.wafflestudio.csereal.core.scholarship.database.ScholarshipRepository
import com.wafflestudio.csereal.core.scholarship.dto.SimpleScholarshipDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface AcademicsService {
    fun createAcademics(studentType: String, postType: String, request: AcademicsDto): AcademicsDto
    fun readAcademics(studentType: String, postType: String): AcademicsDto
    fun createCourse(studentType: String, request: CourseDto): CourseDto
    fun readAllCourses(studentType: String): List<CourseDto>
    fun readCourse(name: String): CourseDto
    fun readScholarship(name: String): ScholarshipPageResponse
}

@Service
class AcademicsServiceImpl(
    private val academicsRepository: AcademicsRepository,
    private val courseRepository: CourseRepository,
    private val scholarshipRepository: ScholarshipRepository
) : AcademicsService {
    @Transactional
    override fun createAcademics(studentType: String, postType: String, request: AcademicsDto): AcademicsDto {
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val enumPostType = makeStringToAcademicsPostType(postType)

        val newAcademics = AcademicsEntity.of(enumStudentType, enumPostType, request)

        academicsRepository.save(newAcademics)

        return AcademicsDto.of(newAcademics)
    }

    @Transactional(readOnly = true)
    override fun readAcademics(studentType: String, postType: String): AcademicsDto {

        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val enumPostType = makeStringToAcademicsPostType(postType)

        val academics = academicsRepository.findByStudentTypeAndPostType(enumStudentType, enumPostType)

        return AcademicsDto.of(academics)
    }

    @Transactional
    override fun createCourse(studentType: String, request: CourseDto): CourseDto {
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        val course = CourseEntity.of(enumStudentType, request)

        courseRepository.save(course)

        return CourseDto.of(course)
    }

    @Transactional(readOnly = true)
    override fun readAllCourses(studentType: String): List<CourseDto> {
        val enumStudentType = makeStringToAcademicsStudentType(studentType)

        val courseDtoList = courseRepository.findAllByStudentTypeOrderByYearAsc(enumStudentType).map {
            CourseDto.of(it)
        }
        return courseDtoList
    }

    @Transactional(readOnly = true)
    override fun readCourse(name: String): CourseDto {
        val course = courseRepository.findByName(name)

        return CourseDto.of(course)
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
