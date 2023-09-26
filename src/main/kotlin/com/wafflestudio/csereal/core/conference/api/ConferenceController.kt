package com.wafflestudio.csereal.core.conference.api

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.conference.dto.ConferenceDto
import com.wafflestudio.csereal.core.conference.dto.ConferenceModifyRequest
import com.wafflestudio.csereal.core.conference.dto.ConferencePage
import com.wafflestudio.csereal.core.conference.service.ConferenceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/api/v1/conference")
@RestController
class ConferenceController(
    private val conferenceService: ConferenceService
) {

    @GetMapping("/page")
    fun getConferencePage(): ResponseEntity<ConferencePage> {
        return ResponseEntity.ok(conferenceService.getConferencePage())
    }

    @AuthenticatedStaff
    @PatchMapping("/page/conferences")
    fun modifyConferencePage(
        @RequestBody conferenceModifyRequest: ConferenceModifyRequest
    ): ResponseEntity<ConferencePage> {
        return ResponseEntity.ok(
            conferenceService.modifyConferences(
                conferenceModifyRequest
            )
        )
    }

    @AuthenticatedStaff
    @PostMapping("/migrate")
    fun migrateConferences(
        @RequestBody requestList: List<ConferenceDto>
    ): ResponseEntity<List<ConferenceDto>> {
        return ResponseEntity.ok(conferenceService.migrateConferences(requestList))
    }
}
