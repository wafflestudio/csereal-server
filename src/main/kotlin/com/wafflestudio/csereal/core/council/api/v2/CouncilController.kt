package com.wafflestudio.csereal.core.council.api.v2

import com.wafflestudio.csereal.core.council.service.CouncilService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/council")
class CouncilController(
    private val councilService: CouncilService
) {
    // ...
}
