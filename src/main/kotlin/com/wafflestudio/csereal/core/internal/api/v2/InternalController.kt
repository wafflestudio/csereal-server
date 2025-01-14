package com.wafflestudio.csereal.core.internal.api.v2

import com.wafflestudio.csereal.common.aop.AuthenticatedStaff
import com.wafflestudio.csereal.core.internal.dto.InternalDto
import com.wafflestudio.csereal.core.internal.service.InternalService
import jakarta.validation.Valid
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
    @AuthenticatedStaff
    fun putInternal(
        @RequestBody @Valid
        req: InternalDto
    ): InternalDto =
        internalService.modifyInternal(req)
}
