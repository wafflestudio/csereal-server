package com.wafflestudio.csereal.core.research.dto

import com.wafflestudio.csereal.core.research.database.ResearchEntity
import java.time.LocalDateTime

data class ResearchDto(
    val id: Long,
    val postType: String,
    val postDetail: String?,
    val title: String,
    val description: String,
    val createdAt: LocalDateTime?,
    val modifiedAt: LocalDateTime?,
    val isPublic: Boolean,
    val labsId: List<Long>?
) {
    companion object {
        fun of(entity: ResearchEntity) = entity.run {
            ResearchDto(
                id = this.id,
                postType = this.postType,
                postDetail = this.postDetail,
                title = this.title,
                description = this.description,
                createdAt = this.createdAt,
                modifiedAt = this.modifiedAt,
                isPublic = this.isPublic,
                labsId = this.labs.map { it.id }
            )
        }
    }
}