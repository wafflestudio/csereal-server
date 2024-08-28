package com.wafflestudio.csereal.core.about.api

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.about.api.req.*
import com.wafflestudio.csereal.core.about.api.res.AboutSearchResBody
import com.wafflestudio.csereal.core.about.dto.*
import com.wafflestudio.csereal.core.about.service.AboutService
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
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

    // read 목록이 하나
    @GetMapping("/{postType}")
    fun readAbout(
        @RequestParam(required = false, defaultValue = "ko") language: String,
        @PathVariable postType: String
    ): ResponseEntity<AboutDto> {
        return ResponseEntity.ok(aboutService.readAbout(language, postType))
    }

    @AuthenticatedStaff
    @PutMapping("/{postType}")
    fun updateAbout(
        @PathVariable postType: String,
        @RequestPart request: UpdateAboutReq,
        @RequestPart newMainImage: MultipartFile?,
        @RequestPart newAttachments: List<MultipartFile>?
    ) = aboutService.updateAbout(postType, request, newMainImage, newAttachments)

    @AuthenticatedStaff
    @PostMapping("/student-clubs")
    fun createClub(
        @RequestPart request: CreateClubReq,
        @RequestPart mainImage: MultipartFile?
    ) = aboutService.createClub(request, mainImage)

    @AuthenticatedStaff
    @PutMapping("/student-clubs/{engName}")
    fun updateClub(
        @PathVariable("engName") name: String,
        @RequestPart request: UpdateDescriptionReq,
        @RequestPart newMainImage: MultipartFile?
    ) = aboutService.updateClub(name, request, newMainImage)

    @AuthenticatedStaff
    @DeleteMapping("/student-clubs/{engName}")
    fun deleteClub(@PathVariable("engName") name: String) = aboutService.deleteClub(name)

    @GetMapping("/student-clubs")
    fun readAllClubs(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<List<StudentClubDto>> {
        return ResponseEntity.ok(aboutService.readAllClubs(language))
    }

    @AuthenticatedStaff
    @PostMapping("/facilities")
    fun createFacilities(@RequestPart request: CreateFacReq, @RequestPart mainImage: MultipartFile?) =
        aboutService.createFacilities(request, mainImage)

    @AuthenticatedStaff
    @PutMapping("/facilities/{id}")
    fun updateFacility(
        @PathVariable id: Long,
        @RequestPart request: CreateFacReq,
        @RequestPart newMainImage: MultipartFile?
    ) = aboutService.updateFacility(id, request, newMainImage)

    @AuthenticatedStaff
    @DeleteMapping("/facilities/{id}")
    fun deleteFacility(@PathVariable id: Long) = aboutService.deleteFacility(id)

    @GetMapping("/facilities")
    fun readAllFacilities(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<List<AboutDto>> {
        return ResponseEntity.ok(aboutService.readAllFacilities(language))
    }

    @PutMapping("/directions/{id}")
    fun updateDirection(@PathVariable id: Long, @RequestBody request: UpdateDescriptionReq) =
        aboutService.updateDirection(id, request)

    @GetMapping("/directions")
    fun readAllDirections(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<List<AboutDto>> {
        return ResponseEntity.ok(aboutService.readAllDirections(language))
    }

    @AuthenticatedStaff
    @PutMapping("/future-careers")
    fun updateFutureCareersPage(@RequestBody request: UpdateDescriptionReq) =
        aboutService.updateFutureCareersPage(request)

    @GetMapping("/future-careers")
    fun readFutureCareers(
        @RequestParam(required = false, defaultValue = "ko") language: String
    ): ResponseEntity<FutureCareersPage> {
        return ResponseEntity.ok(aboutService.readFutureCareers(language))
    }

    @AuthenticatedStaff
    @PostMapping("/future-careers/company")
    fun createCompany(@RequestBody request: CreateCompanyReq) = aboutService.createCompany(request)

    @AuthenticatedStaff
    @PutMapping("/future-careers/company/{id}")
    fun updateCompany(@PathVariable id: Long, @RequestBody request: CreateCompanyReq) =
        aboutService.updateCompany(id, request)

    @AuthenticatedStaff
    @DeleteMapping("/future-careers/company/{id}")
    fun deleteCompany(@PathVariable id: Long) = aboutService.deleteCompany(id)

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
