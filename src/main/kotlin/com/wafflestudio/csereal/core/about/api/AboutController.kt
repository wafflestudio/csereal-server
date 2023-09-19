package com.wafflestudio.csereal.core.about.api

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.about.dto.*
import com.wafflestudio.csereal.core.about.dto.AboutRequest
import com.wafflestudio.csereal.core.about.dto.FutureCareersRequest
import com.wafflestudio.csereal.core.about.service.AboutService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v1/about")
@RestController
class AboutController(
    private val aboutService: AboutService
) {
    // postType: student-clubs / name -> 가디언, 바쿠스, 사커301, 슈타인, 스눕스, 와플스튜디오, 유피넬
    // postType: facilities / name -> 학부-행정실, S-Lab, 소프트웨어-실습실, 하드웨어-실습실, 해동학술정보실, 학생-공간-및-동아리-방, 세미나실, 서버실
    // postType: directions / name -> by-public-transit, by-car, from-far-away

    // Todo: 학부장 인사말(greetings) signature
    @AuthenticatedStaff
    @PostMapping("/{postType}")
    fun createAbout(
        @PathVariable postType: String,
        @Valid
        @RequestPart("request")
        request: AboutDto,
        @RequestPart("mainImage") mainImage: MultipartFile?,
        @RequestPart("attachments") attachments: List<MultipartFile>?
    ): ResponseEntity<AboutDto> {
        return ResponseEntity.ok(aboutService.createAbout(postType, request, mainImage, attachments))
    }

    // read 목록이 하나
    @GetMapping("/{postType}")
    fun readAbout(
        @PathVariable postType: String
    ): ResponseEntity<AboutDto> {
        return ResponseEntity.ok(aboutService.readAbout(postType))
    }

    @GetMapping("/student-clubs")
    fun readAllClubs(): ResponseEntity<List<AboutDto>> {
        return ResponseEntity.ok(aboutService.readAllClubs())
    }

    @GetMapping("/facilities")
    fun readAllFacilities(): ResponseEntity<List<AboutDto>> {
        return ResponseEntity.ok(aboutService.readAllFacilities())
    }

    @GetMapping("/directions")
    fun readAllDirections(): ResponseEntity<List<AboutDto>> {
        return ResponseEntity.ok(aboutService.readAllDirections())
    }

    @GetMapping("/future-careers")
    fun readFutureCareers(): ResponseEntity<FutureCareersPage> {
        return ResponseEntity.ok(aboutService.readFutureCareers())
    }

    @PostMapping("/migrate")
    fun migrateAbout(
        @RequestBody requestList: List<AboutRequest>
    ): ResponseEntity<List<AboutDto>> {
        return ResponseEntity.ok(aboutService.migrateAbout(requestList))
    }

    @PostMapping("/future-careers/migrate")
    fun migrateFutureCareers(
        @RequestBody request: FutureCareersRequest
    ): ResponseEntity<FutureCareersPage> {
        return ResponseEntity.ok(aboutService.migrateFutureCareers(request))
    }

    @PostMapping("/student-clubs/migrate")
    fun migrateStudentClubs(
        @RequestBody requestList: List<StudentClubDto>
    ): ResponseEntity<List<StudentClubDto>> {
        return ResponseEntity.ok(aboutService.migrateStudentClubs(requestList))
    }

    @PostMapping("/facilities/migrate")
    fun migrateFacilities(
        @RequestBody requestList: List<FacilityDto>
    ): ResponseEntity<List<FacilityDto>> {
        return ResponseEntity.ok(aboutService.migrateFacilities(requestList))
    }

    @PostMapping("/directions/migrate")
    fun migrateDirections(
        @RequestBody requestList: List<DirectionDto>
    ): ResponseEntity<List<DirectionDto>> {
        return ResponseEntity.ok(aboutService.migrateDirections(requestList))
    }
}
