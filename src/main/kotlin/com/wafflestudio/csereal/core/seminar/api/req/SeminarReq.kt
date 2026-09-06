package com.wafflestudio.csereal.core.seminar.api.req

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

/** 생성·수정 요청이 공유하는 본문. 엔티티 매핑은 이 타입만 받는다. */
interface SeminarReqBody {
    val title: String
    val titleForMain: String?
    val description: String
    val introduction: String
    val name: String
    val speakerURL: String?
    val speakerTitle: String?
    val affiliation: String
    val affiliationURL: String?
    val startDate: LocalDateTime
    val endDate: LocalDateTime?
    val location: String
    val host: String?
    val additionalNote: String?
    val isPrivate: Boolean
    val isImportant: Boolean
    val importantUntil: LocalDate?
}

data class CreateSeminarReq(
    override val title: String,
    override val titleForMain: String?,
    override val description: String,
    override val introduction: String,
    override val name: String,
    override val speakerURL: String?,
    override val speakerTitle: String?,
    override val affiliation: String,
    override val affiliationURL: String?,
    override val startDate: LocalDateTime,
    override val endDate: LocalDateTime?,
    override val location: String,
    override val host: String?,
    override val additionalNote: String?,
    override val isPrivate: Boolean,
    override val isImportant: Boolean,
    override val importantUntil: LocalDate?
) : SeminarReqBody

data class UpdateSeminarReq(
    override val title: String,
    override val titleForMain: String?,
    override val description: String,
    override val introduction: String,
    override val name: String,
    override val speakerURL: String?,
    override val speakerTitle: String?,
    override val affiliation: String,
    override val affiliationURL: String?,
    override val startDate: LocalDateTime,
    override val endDate: LocalDateTime?,
    override val location: String,
    override val host: String?,
    override val additionalNote: String?,
    override val isPrivate: Boolean,
    override val isImportant: Boolean,
    override val importantUntil: LocalDate?,
    /** 유지할 기존 첨부 id. 여기 없는 첨부는 지운다. */
    val attachmentIds: List<Long>,
    /** 대표 이미지를 지운다. 새 이미지(newMainImage)가 함께 오면 무시된다. */
    @Schema(description = "대표이미지를 뗀다. 새 이미지를 함께 보내면 교체가 우선이라 이 값은 무시된다.")
    val removeImage: Boolean
) : SeminarReqBody
