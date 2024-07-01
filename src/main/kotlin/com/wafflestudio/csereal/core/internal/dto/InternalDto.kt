package com.wafflestudio.csereal.core.internal.dto

import com.wafflestudio.csereal.core.internal.database.InternalEntity
import jakarta.validation.constraints.NotBlank

data class InternalDto(
    @field:NotBlank
    val description: String
) {
    companion object {
        fun from(internal: InternalEntity) = InternalDto(
            description = internal.description
        )
    }
}
