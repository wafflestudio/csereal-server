package com.wafflestudio.csereal.core.academics.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.academics.database.*
import com.wafflestudio.csereal.core.academics.dto.CourseDto
import com.wafflestudio.csereal.core.academics.dto.AcademicsDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface AcademicsService {
    fun createAcademics(studentType: String, postType: String, request: AcademicsDto): AcademicsDto
    fun readAcademics(studentType: String, postType: String): AcademicsDto
    fun createCourse(studentType: String, request: CourseDto): CourseDto
    fun readAllCourses(studentType: String): List<CourseDto>
    fun readCourse(name: String): CourseDto
    fun readScholarship(name:String): AcademicsDto
}

@Service
class AcademicsServiceImpl(
    private val academicsRepository: AcademicsRepository,
    private val courseRepository: CourseRepository,
) : AcademicsService {
    val stringPostTypes = listOf("guide", "general-studies-requirements", "curriculum", "degree-requirements", "course-changes", "scholarship")
    val enumPostTypes = listOf(
        AcademicsPostType.GUIDE,
        AcademicsPostType.GENERAL_STUDIES_REQUIREMENTS,
        AcademicsPostType.CURRICULUM,
        AcademicsPostType.DEGREE_REQUIREMENTS,
        AcademicsPostType.COURSE_CHANGES,
        AcademicsPostType.SCHOLARSHIP
    )

    @Transactional
    override fun createAcademics(studentType: String, postType: String, request: AcademicsDto): AcademicsDto {
        if(!stringPostTypes.contains(postType)) {
            throw CserealException.Csereal404("해당하는 내용을 전송할 수 없습니다.")
        }

        val enumPostType = enumPostTypes[stringPostTypes.indexOf(postType)]
        val newAcademics = AcademicsEntity.of(studentType, enumPostType, request)

        academicsRepository.save(newAcademics)

        return AcademicsDto.of(newAcademics)
    }

    @Transactional(readOnly = true)
    override fun readAcademics(studentType: String, postType: String): AcademicsDto {
        if(!stringPostTypes.contains(postType)) {
            throw CserealException.Csereal404("해당하는 내용을 전송할 수 없습니다.")
        }

        val enumPostType = enumPostTypes[stringPostTypes.indexOf(postType)]

        val academics = academicsRepository.findByStudentTypeAndPostType(studentType, enumPostType)

        return AcademicsDto.of(academics)
    }

    @Transactional
    override fun createCourse(studentType: String, request: CourseDto): CourseDto {
        val course = CourseEntity.of(studentType, request)

        courseRepository.save(course)

        return CourseDto.of(course)
    }

    @Transactional(readOnly = true)
    override fun readAllCourses(studentType: String): List<CourseDto> {
        val courseDtoList = courseRepository.findAllByStudentTypeOrderByYearAsc(studentType).map {
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
    override fun readScholarship(name: String): AcademicsDto {
        val scholarship = academicsRepository.findByName(name)

        return AcademicsDto.of(scholarship)
    }


}