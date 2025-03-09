package com.wafflestudio.csereal.core.internal.api.v2

import com.wafflestudio.csereal.core.internal.dto.InternalDto
import com.wafflestudio.csereal.core.internal.service.InternalService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v2/internal")
class InternalController(
    private val internalService: InternalService
) {
    @GetMapping
    fun getInternal(): InternalDto =
        internalService.getInternal()

    @PutMapping
    @PreAuthorize("hasRole('STAFF')")
    fun putInternal(
        @RequestBody @Valid
        req: InternalDto
    ): InternalDto =
        internalService.modifyInternal(req)
}
