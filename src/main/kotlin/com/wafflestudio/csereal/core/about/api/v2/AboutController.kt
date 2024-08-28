package com.wafflestudio.csereal.core.about.api.v2

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.about.api.req.CreateClubReq
import com.wafflestudio.csereal.core.about.api.req.GroupedClubDto
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
        @RequestPart request: GroupedClubDto,
        @RequestPart newMainImage: MultipartFile?
    ) = aboutService.updateClub(request, newMainImage)

    @AuthenticatedStaff
    @DeleteMapping("/student-clubs/{id}")
    fun deleteClub(@PathVariable id: Long) = aboutService.deleteClub(id)
}
