package com.wafflestudio.csereal.core.undergraduate.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.member.database.EducationEntity
import com.wafflestudio.csereal.core.undergraduate.database.CourseEntity
import com.wafflestudio.csereal.core.undergraduate.database.CourseRepository
import com.wafflestudio.csereal.core.undergraduate.database.UndergraduateEntity
import com.wafflestudio.csereal.core.undergraduate.database.UndergraduateRepository
import com.wafflestudio.csereal.core.undergraduate.dto.CourseDto
import com.wafflestudio.csereal.core.undergraduate.dto.UndergraduateDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface UndergraduateService {
    fun createUndergraduate(postType: String, request: UndergraduateDto): UndergraduateDto
    fun readUndergraduate(postType: String): UndergraduateDto
    /*
    fun createUnderCourseDependency(request: UndergraduateDto): UndergraduateDto
    fun readUnderCourseDependency(): UndergraduateDto
     */
    fun readAllCourses(): List<CourseDto>
    fun createCourse(request: CourseDto): CourseDto
    fun readCourse(title: String): CourseDto
}

@Service
class UndergraduateServiceImpl(
    private val undergraduateRepository: UndergraduateRepository,
    private val courseRepository: CourseRepository,
) : UndergraduateService {
    @Transactional
    override fun createUndergraduate(postType: String, request: UndergraduateDto): UndergraduateDto {
        val newUndergraduate = UndergraduateEntity.of(postType, request)

        undergraduateRepository.save(newUndergraduate)

        return UndergraduateDto.of(newUndergraduate)
    }

    @Transactional(readOnly = true)
    override fun readUndergraduate(postType: String): UndergraduateDto {
        val undergraduate : UndergraduateEntity = undergraduateRepository.findByPostType(postType)
            ?: throw CserealException.Csereal400("존재하지 않는 게시글입니다.")

        return UndergraduateDto.of(undergraduate)
    }

    /*
    @Transactional
    override fun createUnderCourseDependency(request: UndergraduateDto): UndergraduateDto {
        val newUnderCourseDependency = UndergraduateEntity(
            postType = "underCourseDependency",
            title = request.title,
            description = request.description,
            isPublic = request.isPublic
        )

        undergraduateRepository.save(newUnderCourseDependency)

        return UndergraduateDto.of(newUnderCourseDependency)
    }

    @Transactional(readOnly = true)
    override fun readUnderCourseDependency(): UndergraduateDto {
        val undergraduate : UndergraduateEntity = undergraduateRepository.findByPostType("underCourseDependency")

        return UndergraduateDto.of(undergraduate)
    }

     */

    @Transactional
    override fun readAllCourses(): List<CourseDto> {
        val courseDtoList = courseRepository.findAllByOrderByYearAsc().map {
           CourseDto.of(it)
        }
        return courseDtoList
    }
    @Transactional
    override fun createCourse(request: CourseDto): CourseDto {
        val course = CourseEntity.of(request)

        courseRepository.save(course)

        return CourseDto.of(course)
    }

    @Transactional
    override fun readCourse(title: String): CourseDto {
        val course : CourseEntity = courseRepository.findByTitle(title)
            ?: throw CserealException.Csereal400("존재하지 않는 수업입니다.")

        return CourseDto.of(course)
    }

}