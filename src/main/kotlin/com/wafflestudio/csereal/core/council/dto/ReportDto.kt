package com.wafflestudio.csereal.core.council.dto

import com.wafflestudio.csereal.core.council.database.CouncilEntity
import com.wafflestudio.csereal.core.council.database.CouncilType
import java.time.LocalDateTime

data class ReportDto(
    val id: Long,
    val title: String,
    val description: String,
    val imageURL: String?,
    val sequence: Int,
    val name: String,
    val createdAt: LocalDateTime,
    val prevId: Long?,
    val prevTitle: String?,
    val nextId: Long?,
    val nextTitle: String?
) {
    companion object {
        fun of(entity: CouncilEntity, prev: CouncilEntity?, next: CouncilEntity?, imageURL: String?): ReportDto {
            require(entity.type == CouncilType.REPORT) {
                "CouncilEntity must be of type REPORT, but was ${entity.type}"
            }
            return ReportDto(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                imageURL = imageURL,
                sequence = entity.sequence,
                name = entity.name,
                createdAt = entity.createdAt!!,
                prevId = prev?.id,
                prevTitle = prev?.title,
                nextId = next?.id,
                nextTitle = next?.title
            )
        }
    }
}

data class SimpleReportDto(
    val id: Long,
    val title: String,
    val sequence: Int,
    val name: String,
    val createdAt: LocalDateTime,
    val imageURL: String?
) {
    companion object {
        fun of(councilEntity: CouncilEntity, imageURL: String?): SimpleReportDto {
            require(councilEntity.type == CouncilType.REPORT) {
                "CouncilEntity must be of type REPORT, but was ${councilEntity.type}"
            }
            return SimpleReportDto(
                id = councilEntity.id,
                title = councilEntity.title,
                sequence = councilEntity.sequence,
                name = councilEntity.name,
                createdAt = councilEntity.createdAt!!,
                imageURL = imageURL
            )
        }
    }
}

data class ReportListDto(
    val total: Long,
    val reports: List<SimpleReportDto>
)

data class ReportCreateRequest(
    val title: String,
    val description: String,
    val sequence: Int,
    val name: String
)

data class ReportUpdateRequest(
    val title: String,
    val description: String,
    val removeImage: Boolean,
    val sequence: Int,
    val name: String
)
