package com.wafflestudio.csereal.core.council.dto

import com.wafflestudio.csereal.core.council.database.CouncilEntity
import java.time.LocalDateTime

data class ReportDto(
    val id: Long,
    val title: String,
    val description: String,
    val author: String,
    val createdAt: LocalDateTime?,
    val prevId: Long?,
    val prevTitle: String?,
    val nextId: Long?,
    val nextTitle: String?,
) {
    companion object {
        fun of(entity: CouncilEntity, prev: CouncilEntity?, next: CouncilEntity?): ReportDto = ReportDto(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            author = entity.author.name,
            createdAt = entity.createdAt,
            prevId = prev?.id,
            prevTitle = prev?.title,
            nextId = next?.id,
            nextTitle = next?.title
        )
    }
}

data class SimpleReportDto(
    val id: Long,
    val title: String,
    val author: String,
    val createdAt: LocalDateTime?,
    val imageURL: String?
) {
    companion object {
        fun of(councilEntity: CouncilEntity, imageURL: String?): SimpleReportDto = SimpleReportDto(
            id = councilEntity.id,
            title = councilEntity.title,
            author = councilEntity.author.name,
            createdAt = councilEntity.createdAt,
            imageURL = imageURL
        )
    }
}

data class ReportListDto(
    val total: Long,
    val reports: List<SimpleReportDto>
)

data class ReportCreateRequest(
    val title: String,
    val description: String
)

data class ReportUpdateRequest(
    val title: String,
    val description: String,
    val removeImage: Boolean
)
