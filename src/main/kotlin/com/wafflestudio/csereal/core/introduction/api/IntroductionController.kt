package com.wafflestudio.csereal.core.introduction.api

import com.wafflestudio.csereal.core.introduction.dto.IntroductionDto
import com.wafflestudio.csereal.core.introduction.service.IntroductionService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping
@RestController
class IntroductionController(
    private val introductionService: IntroductionService
) {
    // postType -> 학부 소개: about, 연혁: history, 소개책자: CSE_Brochure_kr, 졸업생 진로: career-options,
    // 졸업생 창업 기업: graduate-enterprise, 연락처: contact-us
    // Todo: 전체 image, file, 학부장 인사말 signature
    @PostMapping("/{postType}")
    fun createIntroduction(
        @PathVariable postType: String,
        @Valid @RequestBody request: IntroductionDto
    ) : ResponseEntity<IntroductionDto> {
        return ResponseEntity.ok(introductionService.createIntroduction(postType, null, request))
    }

    // read 목록이 하나
    @GetMapping("/{postType}")
    fun readIntroduction(
        @PathVariable postType: String,
    ) : ResponseEntity<IntroductionDto> {
        return ResponseEntity.ok(introductionService.readIntroduction(postType))
    }

    /*
    @PostMapping("/students-club/{name}")
    fun createClubDetail(
        @PathVariable name: String,
        @Valid @RequestBody request: IntroductionDto
    ) : ResponseEntity<IntroductionDto> {
        return ResponseEntity.ok(introductionService.createIntroduction("students-clubs", name, request))
    }

    @PostMapping("/facility/{name}")
    fun createFacilityDetail(
        @PathVariable name: String,
        @Valid @RequestBody request: IntroductionDto
    ) : ResponseEntity<IntroductionDto> {
        return ResponseEntity.ok(introductionService.createIntroduction("facilities", name, request))
    }

    @PostMapping("/directions/{by}")
    fun createDirectionDetail(
        @PathVariable by: String,
        @Valid @RequestBody request: IntroductionDto
    ) : ResponseEntity<IntroductionDto> {
        return ResponseEntity.ok(introductionService.createIntroduction("directions",by, request))
    }

     */
    // postType: student-clubs / postDetail -> 가디언, 바쿠스, 사커301, 슈타인, 스눕스, 와플스튜디오, 유피넬
    // postType: facilities / postDetail -> 학부-행정실, S-Lab, 소프트웨어-실습실, 하드웨어-실습실, 해동학술정보실, 학생-공간-및-동아리-방, 세미나실, 서버실
    // postType: directions / postDetail -> by-public-transit, by-car, from-far-away
    @PostMapping("/{postType}/{name}")
    fun createIntroductionDetail(
        @PathVariable postType: String,
        @PathVariable name: String,
        @Valid @RequestBody request: IntroductionDto
    ) : ResponseEntity<IntroductionDto> {
        return ResponseEntity.ok(introductionService.createIntroduction(postType, name, request))
    }

    @GetMapping("/students-clubs")
    fun readAllClubs() : ResponseEntity<List<IntroductionDto>> {
        return ResponseEntity.ok(introductionService.readAllClubs())
    }

    @GetMapping("/facilities")
    fun readAllFacilities() : ResponseEntity<List<IntroductionDto>> {
        return ResponseEntity.ok(introductionService.readAllFacilities())
    }


    @GetMapping("/directions")
    fun readAllDirections() : ResponseEntity<List<IntroductionDto>> {
        return ResponseEntity.ok(introductionService.readAllDirections())
    }

}