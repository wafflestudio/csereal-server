package com.wafflestudio.csereal.core.conference.api.v2

import com.wafflestudio.csereal.core.conference.dto.ConferenceModifyRequest
import com.wafflestudio.csereal.core.conference.dto.ConferencePage
import com.wafflestudio.csereal.core.conference.service.ConferenceService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RequestMapping("/api/v2/conference")
@RestController
class ConferenceController(
    private val conferenceService: ConferenceService
) {
    @GetMapping("/page")
    fun getConferencePage(): ResponseEntity<ConferencePage> {
        return ResponseEntity.ok(conferenceService.getConferencePage())
    }

    @PreAuthorize("hasRole('STAFF')")
    @PatchMapping("/page/conferences")
    fun modifyConferencePage(
        @RequestBody conferenceModifyRequest: ConferenceModifyRequest
    ): ResponseEntity<ConferencePage> {
        return ResponseEntity.ok(conferenceService.modifyConferences(conferenceModifyRequest))
    }
}
