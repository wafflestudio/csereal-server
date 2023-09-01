package com.wafflestudio.csereal.core.research.dto

import com.wafflestudio.csereal.core.research.database.ResearchEntity
import com.wafflestudio.csereal.core.research.database.ResearchPostType
import java.time.LocalDateTime

data class ResearchDto(
    val id: Long,
    val postType: ResearchPostType,
    val name: String,
    val description: String?,
    val websiteURL: String?,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val labsId: List<Long>?
) {
    companion object {
        fun of(entity: ResearchEntity) = entity.run {
            ResearchDto(
                id = this.id,
                postType = this.postType,
                name = this.name,
                description = this.description,
                websiteURL = this.websiteURL,
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                labsId = this.labs.map { it.id }
            )
        }
    }
}