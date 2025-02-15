package com.wafflestudio.csereal.core.council.api.v2

import com.wafflestudio.csereal.core.council.dto.*
import com.wafflestudio.csereal.core.council.service.CouncilService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v2/council")
class CouncilController(
    private val councilService: CouncilService
) {
    // TODO: api 제한
    @PostMapping("/report", consumes = ["multipart/form-data"])
    fun createReport(
        @RequestPart
        request: ReportCreateRequest,
        @RequestPart mainImage: MultipartFile?
    ) = councilService.createReport(request, mainImage)

    @GetMapping("/report/{id}")
    fun readReport(@PathVariable id: Long): ReportDto = councilService.readReport(id)

    @GetMapping("/report")
    fun readAllReports(
        @RequestParam(required = false, defaultValue = "1") pageNum: Int,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int
    ): ReportListDto {
        val pageRequest = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        return councilService.readAllReports(pageRequest)
    }

    @PutMapping("/report/{id}", consumes = ["multipart/form-data"])
    fun updateReport(
        @PathVariable id: Long,
        @RequestPart request: ReportUpdateRequest,
        @RequestPart newMainImage: MultipartFile?
    ) = councilService.updateReport(id, request, newMainImage)

    @DeleteMapping("/report/{id}")
    fun deleteReport(
        @PathVariable id: Long
    ) = councilService.deleteReport(id)

    @GetMapping("/intro")
    fun readIntro(): CouncilIntroDto = councilService.readIntro()

    @PutMapping("/intro", consumes = ["multipart/form-data"])
    fun updateIntro(
        @RequestPart request: CouncilIntroUpdateRequest,
        @RequestPart newMainImage: MultipartFile?
    ) = councilService.updateIntro(request, newMainImage)
}
