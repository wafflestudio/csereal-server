package com.wafflestudio.csereal.core.academics.service

import com.wafflestudio.csereal.core.academics.database.CourseEntity
import com.wafflestudio.csereal.core.academics.database.CourseRepository
import com.wafflestudio.csereal.core.academics.database.AcademicsEntity
import com.wafflestudio.csereal.core.academics.database.AcademicsRepository
import com.wafflestudio.csereal.core.academics.dto.CourseDto
import com.wafflestudio.csereal.core.academics.dto.AcademicsDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface AcademicsService {
    fun createAcademics(to: String, request: AcademicsDto): AcademicsDto
    fun readAcademics(to: String, postType: String): AcademicsDto

    /*
    fun createUnderCourseDependency(request: AcademicsDto): AcademicsDto
    fun readUnderCourseDependency(): AcademicsDto
     */
    fun readAllCourses(to: String): List<CourseDto>
    fun createCourse(to: String, request: CourseDto): CourseDto
    fun readCourse(name: String): CourseDto
    fun readScholarship(name:String): AcademicsDto
}

@Service
class AcademicsServiceImpl(
    private val academicsRepository: AcademicsRepository,
    private val courseRepository: CourseRepository,
) : AcademicsService {
    @Transactional
    override fun createAcademics(to: String, request: AcademicsDto): AcademicsDto {
        val newAcademics = AcademicsEntity.of(to, request)

        academicsRepository.save(newAcademics)

        return AcademicsDto.of(newAcademics)
    }

    @Transactional(readOnly = true)
    override fun readAcademics(to: String, postType: String): AcademicsDto {
        val academics : AcademicsEntity = academicsRepository.findByToAndPostType(to, postType)

        return AcademicsDto.of(academics)
    }

    /*
    @Transactional
    override fun createUnderCourseDependency(request: AcademicsDto): AcademicsDto {
        val newUnderCourseDependency = AcademicsEntity(
            postType = "underCourseDependency",
            title = request.title,
            description = request.description,
            isPublic = request.isPublic
        )

        AcademicsRepository.save(newUnderCourseDependency)

        return AcademicsDto.of(newUnderCourseDependency)
    }

    @Transactional(readOnly = true)
    override fun readUnderCourseDependency(): AcademicsDto {
        val Academics : AcademicsEntity = AcademicsRepository.findByPostType("underCourseDependency")

        return AcademicsDto.of(Academics)
    }

     */

    @Transactional
    override fun readAllCourses(to: String): List<CourseDto> {
        val courseDtoList = courseRepository.findAllByToOrderByYearAsc(to).map {
           CourseDto.of(it)
        }
        return courseDtoList
    }
    @Transactional
    override fun createCourse(to: String, request: CourseDto): CourseDto {
        val course = CourseEntity.of(to, request)

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