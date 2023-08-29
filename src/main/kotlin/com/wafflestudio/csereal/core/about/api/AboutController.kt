package com.wafflestudio.csereal.core.about.api

import com.wafflestudio.csereal.core.about.dto.AboutDto
import com.wafflestudio.csereal.core.about.service.AboutService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/about")
@RestController
class AboutController(
    private val aboutService: AboutService
) {
    // postType: student-clubs / name -> 가디언, 바쿠스, 사커301, 슈타인, 스눕스, 와플스튜디오, 유피넬
    // postType: facilities / name -> 학부-행정실, S-Lab, 소프트웨어-실습실, 하드웨어-실습실, 해동학술정보실, 학생-공간-및-동아리-방, 세미나실, 서버실
    // postType: directions / name -> by-public-transit, by-car, from-far-away

    // Todo: 전체 image, file, 학부장 인사말(greetings) signature
    @PostMapping("/{postType}")
    fun createAbout(
        @PathVariable postType: String,
        @Valid @RequestPart("request") request: AboutDto,
        @RequestPart("image") image: MultipartFile?,
        @RequestPart("attachments") attachments: List<MultipartFile>?,
    ) : ResponseEntity<AboutDto> {
        return ResponseEntity.ok(aboutService.createAbout(postType, request, image, attachments))
    }

    // read 목록이 하나
    @GetMapping("/{postType}")
    fun readAbout(
        @PathVariable postType: String,
    ) : ResponseEntity<AboutDto> {
        return ResponseEntity.ok(aboutService.readAbout(postType))
    }

    @GetMapping("/student-clubs")
    fun readAllClubs() : ResponseEntity<List<AboutDto>> {
        return ResponseEntity.ok(aboutService.readAllClubs())
    }

    @GetMapping("/facilities")
    fun readAllFacilities() : ResponseEntity<List<AboutDto>> {
        return ResponseEntity.ok(aboutService.readAllFacilities())
    }


    @GetMapping("/directions")
    fun readAllDirections() : ResponseEntity<List<AboutDto>> {
        return ResponseEntity.ok(aboutService.readAllDirections())
    }

}