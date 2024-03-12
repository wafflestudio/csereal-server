package com.wafflestudio.csereal.core.academics.api

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.academics.dto.*
import com.wafflestudio.csereal.core.academics.service.AcademicsService
import com.wafflestudio.csereal.core.academics.dto.ScholarshipDto
import com.wafflestudio.csereal.core.academics.service.AcademicsSearchService
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v1/academics")
@RestController
class AcademicsController(
    private val academicsService: AcademicsService,
    private val academicsSearchService: AcademicsSearchService
) {
    @AuthenticatedStaff
    @PostMapping("/{studentType}/{postType}")
    fun createAcademics(
        @PathVariable studentType: String,
        @PathVariable postType: String,
        @Valid
        @RequestPart("request")
        request: AcademicsDto,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ): ResponseEntity<AcademicsDto> {
        return ResponseEntity.ok(
            academicsService.createAcademics(studentType, postType, request, attachments)
        )
    }

    @GetMapping("/{studentType}/guide")
    fun readGuide(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @PathVariable studentType: String
    ): ResponseEntity<GuidePageResponse> {
        return ResponseEntity.ok(academicsService.readGuide(language, studentType))
    }

    @GetMapping("/undergraduate/general-studies-requirements")
    fun readGeneralStudiesRequirements(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<GeneralStudiesRequirementsPageResponse> {
        return ResponseEntity.ok(academicsService.readGeneralStudiesRequirements(language))
    }

    @GetMapping("/{studentType}/{postType}")
    fun readAcademicsYearResponses(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @PathVariable studentType: String,
        @PathVariable postType: String
    ): ResponseEntity<List<AcademicsYearResponse>> {
        return ResponseEntity.ok(
            academicsService.readAcademicsYearResponses(language, studentType, postType)
        )
    }

    //교과목 정보
    @AuthenticatedStaff
    @PostMapping("/{studentType}/course")
    fun createCourse(
        @PathVariable studentType: String,
        @Valid
        @RequestPart("request")
        request: CourseDto,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ): ResponseEntity<CourseDto> {
        return ResponseEntity.ok(academicsService.createCourse(studentType, request, attachments))
    }

    @GetMapping("/{studentType}/courses")
    fun readAllCourses(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @PathVariable studentType: String
    ): ResponseEntity<List<CourseDto>> {
        return ResponseEntity.ok(academicsService.readAllCourses(language, studentType))
    }

    @GetMapping("/course")
    fun readCourse(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @RequestParam name: String
    ): ResponseEntity<CourseDto> {
        return ResponseEntity.ok(academicsService.readCourse(language, name))
    }

    @GetMapping("/undergraduate/degree-requirements")
    fun readDegreeRequirements(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<DegreeRequirementsPageResponse> {
        return ResponseEntity.ok(academicsService.readDegreeRequirements(language))
    }

    @AuthenticatedStaff
    @PostMapping("/{studentType}/scholarshipDetail")
    fun createScholarshipDetail(
        @PathVariable studentType: String,
        @Valid @RequestBody
        request: ScholarshipDto
    ): ResponseEntity<ScholarshipDto> {
        return ResponseEntity.ok(academicsService.createScholarshipDetail(studentType, request))
    }

    @GetMapping("/{studentType}/scholarship")
    fun readAllScholarship(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @PathVariable studentType: String
    ): ResponseEntity<ScholarshipPageResponse> {
        return ResponseEntity.ok(academicsService.readAllScholarship(language, studentType))
    }

    @GetMapping("/scholarship/{scholarshipId}")
    fun getScholarship(
        @PathVariable scholarshipId: Long
    ): ResponseEntity<ScholarshipDto> {
        return ResponseEntity.ok(academicsService.readScholarship(scholarshipId))
    }

    @PostMapping("/{studentType}/{postType}/migrate")
    fun migrateAcademicsDetail(
        @PathVariable studentType: String,
        @PathVariable postType: String,
        @RequestBody requestList: List<AcademicsDto>
    ): ResponseEntity<List<AcademicsDto>> {
        return ResponseEntity.ok(
            academicsService.migrateAcademicsDetail(studentType, postType, requestList)
        )
    }

    @PostMapping("/course/migrate/{studentType}")
    fun migrateCourses(
        @PathVariable studentType: String,
        @RequestBody requestList: List<CourseDto>
    ): ResponseEntity<List<CourseDto>> {
        return ResponseEntity.ok(academicsService.migrateCourses(studentType, requestList))
    }

    @PostMapping("/{studentType}/scholarshipDetail/migrate")
    fun migrateScholarshipDetail(
        @PathVariable studentType: String,
        @RequestBody requestList: List<ScholarshipDto>
    ): ResponseEntity<List<ScholarshipDto>> {
        return ResponseEntity.ok(
            academicsService.migrateScholarshipDetail(studentType, requestList)
        )
    }

    @PatchMapping("/migrateAttachment/{academicsId}")
    fun migrateAcademicsDetailAttachments(
        @PathVariable academicsId: Long,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ): ResponseEntity<AcademicsDto> {
        return ResponseEntity.ok(
            academicsService.migrateAcademicsDetailAttachments(
                academicsId,
                attachments
            )
        )
    }

    @GetMapping("/search/top")
    fun searchTop(
        @RequestParam(required = true) keyword: String,
        @RequestParam(required = true) @Valid @Positive number: Int,
        @RequestParam(required = true, defaultValue = "ko") language: String,
        @RequestParam(required = false, defaultValue = "30") amount: Int
    ) = academicsSearchService.searchTopAcademics(
        keyword = keyword,
        language = LanguageType.makeStringToLanguageType(language),
        number = number,
        amount = amount
    )

    @GetMapping("/search")
    fun searchAcademics(
        @RequestParam(required = true) keyword: String,
        @RequestParam(required = true) @Valid @Positive pageSize: Int,
        @RequestParam(required = true) @Valid @Positive pageNum: Int,
        @RequestParam(required = true, defaultValue = "ko") language: String,
        @RequestParam(required = false, defaultValue = "30") amount: Int
    ) = academicsSearchService.searchAcademics(
        keyword = keyword,
        language = LanguageType.makeStringToLanguageType(language),
        pageSize = pageSize,
        pageNum = pageNum,
        amount = amount
    )
}
