package com.wafflestudio.csereal.core.internal.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.sanitize.HtmlContentHolder
import com.wafflestudio.csereal.common.sanitize.HtmlField
import com.wafflestudio.csereal.core.internal.dto.InternalDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.validation.constraints.NotBlank

@Entity(name = "internal")
class InternalEntity(
    @NotBlank
    @Column(columnDefinition = "TEXT")
    var description: String
) : BaseTimeEntity(), HtmlContentHolder {

    override fun htmlFields() = listOf(HtmlField({ description }, { description = it }))

    fun update(dto: InternalDto) {
        description = dto.description
    }

    companion object {
        fun of(dto: InternalDto) = InternalEntity(dto.description)
    }
}
