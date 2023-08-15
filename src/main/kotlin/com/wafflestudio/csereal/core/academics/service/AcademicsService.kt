package com.wafflestudio.csereal.core.academics.service

import com.wafflestudio.csereal.core.academics.database.*
import com.wafflestudio.csereal.core.academics.dto.CourseDto
import com.wafflestudio.csereal.core.academics.dto.AcademicsDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface AcademicsService {
    fun createAcademics(studentType: StudentType, request: AcademicsDto): AcademicsDto
    fun readAcademics(studentType: StudentType, postType: String): AcademicsDto
    fun readAllCourses(studentType: StudentType): List<CourseDto>
    fun createCourse(studentType: StudentType, request: CourseDto): CourseDto
    fun readCourse(name: String): CourseDto
    fun readScholarship(name:String): AcademicsDto
}

@Service
class AcademicsServiceImpl(
    private val academicsRepository: AcademicsRepository,
    private val courseRepository: CourseRepository,
) : AcademicsService {
    @Transactional
    override fun createAcademics(studentType: StudentType, request: AcademicsDto): AcademicsDto {
        val newAcademics = AcademicsEntity.of(studentType, request)

        academicsRepository.save(newAcademics)

        return AcademicsDto.of(newAcademics)
    }

    @Transactional(readOnly = true)
    override fun readAcademics(studentType: StudentType, postType: String): AcademicsDto {
        val academics : AcademicsEntity = academicsRepository.findByStudentTypeAndPostType(studentType, postType)

        return AcademicsDto.of(academics)
    }

    @Transactional
    override fun readAllCourses(studentType: StudentType): List<CourseDto> {
        val courseDtoList = courseRepository.findAllByStudentTypeOrderByYearAsc(studentType).map {
           CourseDto.of(it)
        }
        return courseDtoList
    }
    @Transactional
    override fun createCourse(studentType: StudentType, request: CourseDto): CourseDto {
        val course = CourseEntity.of(studentType, request)

        courseRepository.save(course)

        return CourseDto.of(course)
    }

    @Transactional
    override fun readCourse(name: String): CourseDto {
        val course : CourseEntity = courseRepository.findByName(name)

        return CourseDto.of(course)
    }

    @Transactional
    override fun readScholarship(name: String): AcademicsDto {
        val scholarship : AcademicsEntity = academicsRepository.findByName(name)

        return AcademicsDto.of(scholarship)
    }


}