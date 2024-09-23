package com.wafflestudio.csereal.core.about.api.v2

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.about.api.req.*
import com.wafflestudio.csereal.core.about.dto.GroupedClubDto
import com.wafflestudio.csereal.core.about.service.AboutService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RequestMapping("/api/v2/about")
@RestController
class AboutController(
    private val aboutService: AboutService
) {
    @GetMapping("/student-clubs")
    fun readAllClubs(): List<GroupedClubDto> = aboutService.readAllGroupedClubs()

    @AuthenticatedStaff
    @PostMapping("/student-clubs")
    fun createClub(
        @RequestPart request: CreateClubReq,
        @RequestPart mainImage: MultipartFile?
    ) = aboutService.createClub(request, mainImage)

    @AuthenticatedStaff
    @PutMapping("/student-clubs")
    fun updateClub(
        @RequestPart request: UpdateClubReq,
        @RequestPart newMainImage: MultipartFile?
    ) = aboutService.updateClub(request, newMainImage)

    @AuthenticatedStaff
    @DeleteMapping("/student-clubs/{id}")
    fun deleteClub(@PathVariable id: Long) = aboutService.deleteClub(id)

    @AuthenticatedStaff
    @PutMapping("/{postType}")
    fun updateAbout(
        @PathVariable postType: String,
        @RequestPart request: UpdateAboutReq,
        @RequestPart newMainImage: MultipartFile?,
        @RequestPart newAttachments: List<MultipartFile>?
    ) = aboutService.updateAbout(postType, request, newMainImage, newAttachments)

    @AuthenticatedStaff
    @PostMapping("/facilities")
    fun createFacilities(@RequestPart request: CreateFacReq, @RequestPart mainImage: MultipartFile?) =
        aboutService.createFacilities(request, mainImage)

    @AuthenticatedStaff
    @PutMapping("/facilities/{id}")
    fun updateFacility(
        @PathVariable id: Long,
        @RequestPart request: UpdateFacReq,
        @RequestPart newMainImage: MultipartFile?
    ) = aboutService.updateFacility(id, request, newMainImage)

    @AuthenticatedStaff
    @DeleteMapping("/facilities/{id}")
    fun deleteFacility(@PathVariable id: Long) = aboutService.deleteFacility(id)

    @AuthenticatedStaff
    @PutMapping("/directions/{id}")
    fun updateDirection(@PathVariable id: Long, @RequestBody request: UpdateDescriptionReq) =
        aboutService.updateDirection(id, request)

    @AuthenticatedStaff
    @PutMapping("/future-careers")
    fun updateFutureCareersPage(@RequestBody request: UpdateDescriptionReq) =
        aboutService.updateFutureCareersPage(request)

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
}
