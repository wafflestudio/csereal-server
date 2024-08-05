package com.wafflestudio.csereal.core.academics.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.academics.api.req.CreateYearReq
import com.wafflestudio.csereal.core.academics.api.req.UpdateSingleReq
import com.wafflestudio.csereal.core.academics.api.req.UpdateYearReq
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

    fun readGeneralStudiesRequirements(language: String): GeneralStudiesRequirementsPageResponse
    fun readDegreeRequirements(language: String): DegreeRequirementsPageResponse
    fun updateDegreeRequirements(language: String, request: UpdateSingleReq, newAttachments: List<MultipartFile>?)
    fun createCourse(
        studentType: String,
        request: GroupedCourseDto
    )

    fun readAllCourses(language: String, studentType: String): List<CourseDto>
    fun readAllGroupedCourses(studentType: String): List<GroupedCourseDto>
    fun createScholarshipDetail(
        studentType: String,
        request: ScholarshipDto
    ): ScholarshipDto

    fun readAllScholarship(language: String, studentType: String): ScholarshipPageResponse
    fun readScholarship(scholarshipId: Long): ScholarshipDto
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
    private val scholarshipRepository: ScholarshipRepository
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
    override fun readGeneralStudiesRequirements(language: String): GeneralStudiesRequirementsPageResponse {
        val enumLanguageType = LanguageType.makeStringToLanguageType(language)
        val overview =
            academicsRepository.findByLanguageAndStudentTypeAndPostTypeAndYear(
                enumLanguageType,
                AcademicsStudentType.UNDERGRADUATE,
                AcademicsPostType.GENERAL_STUDIES_REQUIREMENTS,
                null
            ) ?: throw CserealException.Csereal404("General Studies Requirements Not Found")
        val generalStudiesEntity =
            academicsRepository.findAllByLanguageAndStudentTypeAndPostTypeOrderByYearDesc(
                enumLanguageType,
                AcademicsStudentType.UNDERGRADUATE,
                AcademicsPostType.GENERAL_STUDIES_REQUIREMENTS
            ).filter { academicsEntity -> academicsEntity.year != null }
        return GeneralStudiesRequirementsPageResponse.of(overview, generalStudiesEntity)
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
    override fun createCourse(
        studentType: String,
        request: GroupedCourseDto
    ) {
        if (courseRepository.existsByCode(request.code)) {
            throw CserealException.Csereal409("해당 교과목 번호를 가지고 있는 엔티티가 이미 있습니다")
        }

        val enumStudentType = makeStringToAcademicsStudentType(studentType)

        val koCourse = CourseEntity.of(
            enumStudentType,
            LanguageType.KO,
            request.ko.classification,
            request.code,
            request.ko.name,
            request.credit,
            request.grade,
            request.ko.description
        )
        val enCourse = CourseEntity.of(
            enumStudentType,
            LanguageType.EN,
            request.en.classification,
            request.code,
            request.en.name,
            request.credit,
            request.grade,
            request.en.description
        )

        // create search data
        koCourse.apply {
            academicsSearch = AcademicsSearchEntity.create(this)
        }
        enCourse.apply {
            academicsSearch = AcademicsSearchEntity.create(this)
        }
        courseRepository.save(koCourse)
        courseRepository.save(enCourse)
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
    override fun readAllGroupedCourses(studentType: String): List<GroupedCourseDto> {
        val enumStudentType = makeStringToAcademicsStudentType(studentType)
        return courseRepository.findGroupedCourses(enumStudentType).map(CourseMapper::toGroupedCourseDTO)
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
            ) ?: throw CserealException.Csereal404("Scholarship Entity Not Found")
        val scholarshipEntityList = scholarshipRepository.findAllByStudentType(enumStudentType)

        return ScholarshipPageResponse.of(academicsEntity, scholarshipEntityList)
    }

    @Transactional(readOnly = true)
    override fun readScholarship(scholarshipId: Long): ScholarshipDto {
        val scholarship = scholarshipRepository.findByIdOrNull(scholarshipId)
            ?: throw CserealException.Csereal404("해당하는 장학제도를 찾을 수 없습니다")
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
