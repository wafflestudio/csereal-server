package com.wafflestudio.csereal.core.conference.api

import com.wafflestudio.csereal.core.conference.dto.ConferencePage
import com.wafflestudio.csereal.core.conference.service.ConferenceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/conference")
@RestController
class ConferenceController(
    private val conferenceService: ConferenceService
) {

    @GetMapping("/page")
    fun getConferencePage(): ResponseEntity<ConferencePage> {
        return ResponseEntity.ok(conferenceService.getConferencePage())
    }

}
