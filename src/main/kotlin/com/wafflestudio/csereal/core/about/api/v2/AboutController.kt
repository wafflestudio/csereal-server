package com.wafflestudio.csereal.core.about.api.v2

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.about.database.AboutSinglePostType
import com.wafflestudio.csereal.core.about.api.req.*
import com.wafflestudio.csereal.core.about.dto.AboutDto
import com.wafflestudio.csereal.core.about.dto.FutureCareersPage
import com.wafflestudio.csereal.core.about.dto.GroupedClubDto
import com.wafflestudio.csereal.core.about.service.AboutService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v2/about")
@RestController
class AboutController(
    private val aboutService: AboutService
) {
    // postType: student-clubs / name -> 가디언, 바쿠스, 사커301, 슈타인, 스눕스, 와플스튜디오, 유피넬
    // postType: facilities / name -> 학부-행정실, S-Lab, 소프트웨어-실습실, 하드웨어-실습실, 해동학술정보실, 학생-공간-및-동아리-방, 세미나실, 서버실
    // postType: directions / name -> by-public-transit, by-car, from-far-away

    // 이 경로가 서빙할 수 있는 넷만 받는다 — 나머지 넷은 전용 경로가 이긴다.
    // 자세한 이유는 AboutSinglePostType 주석 참고.
    @GetMapping("/{postType}")
    fun readAbout(
        @RequestParam(required = false, defaultValue = "ko") language: LanguageType,
        @PathVariable postType: AboutSinglePostType
    ): ResponseEntity<AboutDto> {
        return ResponseEntity.ok(aboutService.readAbout(language, postType.postType))
    }

    @GetMapping("/student-clubs")
    fun readAllClubs(): List<GroupedClubDto> = aboutService.readAllGroupedClubs()

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/student-clubs", consumes = ["multipart/form-data"])
    fun createClub(
        @RequestPart request: CreateClubReq,
        @RequestPart mainImage: MultipartFile?
    ) = aboutService.createClub(request, mainImage)

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/student-clubs", consumes = ["multipart/form-data"])
    fun updateClub(
        @RequestPart request: UpdateClubReq,
        @RequestPart newMainImage: MultipartFile?
    ) = aboutService.updateClub(request, newMainImage)

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/student-clubs/{id}")
    fun deleteClub(@PathVariable id: Long) = aboutService.deleteClub(id)

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{postType}", consumes = ["multipart/form-data"])
    fun updateAbout(
        @PathVariable postType: AboutSinglePostType,
        @RequestPart request: UpdateAboutReq,
        @RequestPart newMainImage: MultipartFile?,
        @RequestPart attachments: List<MultipartFile>?
    ) = aboutService.updateAbout(postType.postType, request, newMainImage, attachments)

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/facilities", consumes = ["multipart/form-data"])
    fun createFacilities(@RequestPart request: CreateFacReq, @RequestPart mainImage: MultipartFile?) =
        aboutService.createFacilities(request, mainImage)

    @GetMapping("/facilities")
    fun readAllGroupedFacilities() = aboutService.readAllGroupedFacilities()

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/facilities/{id}", consumes = ["multipart/form-data"])
    fun updateFacility(
        @PathVariable id: Long,
        @RequestPart request: UpdateFacReq,
        @RequestPart newMainImage: MultipartFile?
    ) = aboutService.updateFacility(id, request, newMainImage)

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/facilities/{id}")
    fun deleteFacility(@PathVariable id: Long) = aboutService.deleteFacility(id)

    @GetMapping("/directions")
    fun readAllGroupedDirections() = aboutService.readAllGroupedDirections()

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/directions/{id}")
    fun updateDirection(@PathVariable id: Long, @RequestBody request: UpdateDescriptionReq) =
        aboutService.updateDirection(id, request)

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/future-careers/stats")
    fun createStats(@RequestBody request: CreateStatReq) = aboutService.createFutureCareersStat(request)

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/future-careers/stats")
    fun updateStats(@RequestBody request: CreateStatReq) = aboutService.updateFutureCareersStat(request)

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/future-careers")
    fun updateFutureCareersPage(@RequestBody request: UpdateDescriptionReq) =
        aboutService.updateFutureCareersPage(request)

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/future-careers/company")
    fun createCompany(@RequestBody request: CreateCompanyReq) = aboutService.createCompany(request)

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/future-careers/company/{id}")
    fun updateCompany(@PathVariable id: Long, @RequestBody request: CreateCompanyReq) =
        aboutService.updateCompany(id, request)

    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/future-careers/company/{id}")
    fun deleteCompany(@PathVariable id: Long) = aboutService.deleteCompany(id)

    @GetMapping("/future-careers")
    fun readFutureCareers(
        @RequestParam(required = false, defaultValue = "ko") language: LanguageType
    ): ResponseEntity<FutureCareersPage> {
        return ResponseEntity.ok(aboutService.readFutureCareers(language))
    }
}
