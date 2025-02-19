package com.wafflestudio.csereal.core.council.api.v2

import com.wafflestudio.csereal.core.council.dto.*
import com.wafflestudio.csereal.core.council.api.res.CouncilFileRuleResponse
import com.wafflestudio.csereal.core.council.api.res.CouncilFileRulesResponse
import com.wafflestudio.csereal.core.council.api.res.CouncilFileMeetingMinuteResponse
import com.wafflestudio.csereal.core.council.service.CouncilFileService
import com.wafflestudio.csereal.core.council.service.CouncilService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import com.wafflestudio.csereal.core.council.type.CouncilFileRulesKey

// TODO: api 제한
@RestController
@RequestMapping("/api/v2/council")
class CouncilController(
    private val councilService: CouncilService,
    private val councilFileService: CouncilFileService
) {
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
    fun upsertIntro(
        @RequestPart request: CouncilIntroUpdateRequest,
        @RequestPart newMainImage: MultipartFile?
    ) = councilService.upsertIntro(request, newMainImage)

    @GetMapping("/rule")
    fun getRule(): CouncilFileRulesResponse =
        councilFileService
            .getCouncilRules()
            .let { CouncilFileRulesResponse.from(it) }

    @GetMapping("/rule/{type}")
    fun getRuleByType(
        @PathVariable(required = true) type: String
    ): CouncilFileRuleResponse =
        councilFileService
            .getCouncilRule(CouncilFileRulesKey.from(type))
            .let { CouncilFileRuleResponse.from(it) }

    @PostMapping("/rule/{type}", consumes = ["multipart/form-data"])
    fun createRuleByType(
        @PathVariable(required = true) type: String,
        @RequestPart("attachments") attachments: List<MultipartFile>
    ): CouncilFileRuleResponse =
        councilFileService
            .createCouncilRule(CouncilFileRulesKey.from(type), attachments)
            .let { CouncilFileRuleResponse.from(it) }

    @PutMapping("/rule/{type}", consumes = ["multipart/form-data"])
    fun updateRuleByType(
        @PathVariable(required = true) type: String,
        @RequestPart("removeFileIds") removeFileIds: List<Long>,
        @RequestPart("addFiles") addFiles: List<MultipartFile>
    ): CouncilFileRuleResponse =
        councilFileService
            .updateCouncilRule(CouncilFileRulesKey.from(type), removeFileIds, addFiles)
            .let { CouncilFileRuleResponse.from(it) }

    @DeleteMapping("/rule/{type}")
    fun deleteRuleByType(
        @PathVariable(required = true) type: String
    ) {
        councilFileService.deleteCouncilRule(CouncilFileRulesKey.from(type))
    }

    @GetMapping("/meeting-minute/{year}")
    fun getMeetingMinutesOfYear(
        @PathVariable(required = true) year: Int
    ): List<CouncilFileMeetingMinuteResponse> =
        councilFileService
            .getCouncilMeetingMinutes(year)
            .map { CouncilFileMeetingMinuteResponse.from(it) }

    @GetMapping("/meeting-minute/{year}/{index}")
    fun getMeetingMinute(
        @PathVariable(required = true) year: Int,
        @PathVariable(required = true) index: Int
    ): CouncilFileMeetingMinuteResponse =
        councilFileService
            .getCouncilMeetingMinute(year, index)
            .let { CouncilFileMeetingMinuteResponse.from(it) }

    @PostMapping("/meeting-minute/{year}", consumes = ["multipart/form-data"])
    fun createMeetingMinute(
        @PathVariable(required = true) year: Int,
        @RequestPart("attachments") attachments: List<MultipartFile>
    ): CouncilFileMeetingMinuteResponse =
        councilFileService
            .createCouncilMeetingMinute(year, attachments)
            .let { CouncilFileMeetingMinuteResponse.from(it) }

    @PutMapping("/meeting-minute/{year}/{index}", consumes = ["multipart/form-data"])
    fun updateMeetingMinute(
        @PathVariable(required = true) year: Int,
        @PathVariable(required = true) index: Int,
        @RequestPart("removeFileIds") removeFileIds: List<Long>,
        @RequestPart("addFiles") addFiles: List<MultipartFile>
    ): CouncilFileMeetingMinuteResponse =
        councilFileService
            .updateCouncilMeetingMinute(year, index, removeFileIds, addFiles)
            .let { CouncilFileMeetingMinuteResponse.from(it) }

    @DeleteMapping("/meeting-minute/{year}/{index}")
    fun deleteMeetingMinute(
        @PathVariable(required = true) year: Int,
        @PathVariable(required = true) index: Int
    ) {
        councilFileService.deleteCouncilMeetingMinute(year, index)
    }
}
