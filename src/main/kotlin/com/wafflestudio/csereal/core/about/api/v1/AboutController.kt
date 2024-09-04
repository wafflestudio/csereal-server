package com.wafflestudio.csereal.core.about.api.v1

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.about.api.req.*
import com.wafflestudio.csereal.core.about.api.res.AboutSearchResBody
import com.wafflestudio.csereal.core.about.dto.*
import com.wafflestudio.csereal.core.about.service.AboutService
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/api/v1/about")
@RestController("AboutControllerV1")
class AboutController(
    private val aboutService: AboutService
) {
    // postType: student-clubs / name -> 가디언, 바쿠스, 사커301, 슈타인, 스눕스, 와플스튜디오, 유피넬
    // postType: facilities / name -> 학부-행정실, S-Lab, 소프트웨어-실습실, 하드웨어-실습실, 해동학술정보실, 학생-공간-및-동아리-방, 세미나실, 서버실
    // postType: directions / name -> by-public-transit, by-car, from-far-away

    // Todo: 학부장 인사말(greetings) signature

    // read 목록이 하나
    @GetMapping("/{postType}")
    fun readAbout(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @PathVariable postType: String
    ): ResponseEntity<AboutDto> {
        return ResponseEntity.ok(aboutService.readAbout(language, postType))
    }

    @Deprecated("Use V2 API")
    @GetMapping("/student-clubs")
    fun readAllClubs(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<List<StudentClubDto>> {
        return ResponseEntity.ok(aboutService.readAllClubs(language))
    }

    @GetMapping("/facilities")
    fun readAllFacilities(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<List<AboutDto>> {
        return ResponseEntity.ok(aboutService.readAllFacilities(language))
    }

    @GetMapping("/directions")
    fun readAllDirections(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<List<AboutDto>> {
        return ResponseEntity.ok(aboutService.readAllDirections(language))
    }

    @GetMapping("/future-careers")
    fun readFutureCareers(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<FutureCareersPage> {
        return ResponseEntity.ok(aboutService.readFutureCareers(language))
    }

    @GetMapping("/search/top")
    fun searchTopAbout(
        @RequestParam(required = true) keyword: String,
        @RequestParam(required = true) @Valid @Positive number: Int,
        @RequestParam(required = true, defaultValue = "ko") language: String,
        @RequestParam(required = false, defaultValue = "30") @Valid @Positive amount: Int
    ): AboutSearchResBody = aboutService.searchTopAbout(
        keyword,
        LanguageType.makeStringToLanguageType(language),
        number,
        amount
    )

    @GetMapping("/search")
    fun searchPageAbout(
        @RequestParam(required = true) keyword: String,
        @RequestParam(required = true) @Valid @Positive pageNum: Int,
        @RequestParam(required = true) @Valid @Positive pageSize: Int,
        @RequestParam(required = true, defaultValue = "ko") language: String,
        @RequestParam(required = false, defaultValue = "30") @Valid @Positive amount: Int
    ): AboutSearchResBody = aboutService.searchPageAbout(
        keyword,
        LanguageType.makeStringToLanguageType(language),
        pageSize,
        pageNum,
        amount
    )
}
