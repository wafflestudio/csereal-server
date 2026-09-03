package com.wafflestudio.csereal.core.notice.api.req

import java.time.LocalDate

/** 생성·수정 요청이 공유하는 본문. 엔티티 매핑은 이 타입만 받는다. */
interface NoticeReqBody {
    val title: String
    val titleForMain: String?
    val description: String
    val isPrivate: Boolean
    val isPinned: Boolean
    val pinnedUntil: LocalDate?
    val isImportant: Boolean
    val importantUntil: LocalDate?
    val tags: List<String>
}

data class CreateNoticeReq(
    override val title: String,
    override val titleForMain: String?,
    override val description: String,
    override val isPrivate: Boolean,
    override val isPinned: Boolean,
    override val pinnedUntil: LocalDate?,
    override val isImportant: Boolean,
    override val importantUntil: LocalDate?,
    override val tags: List<String>
) : NoticeReqBody

data class UpdateNoticeReq(
    override val title: String,
    override val titleForMain: String?,
    override val description: String,
    override val isPrivate: Boolean,
    override val isPinned: Boolean,
    override val pinnedUntil: LocalDate?,
    override val isImportant: Boolean,
    override val importantUntil: LocalDate?,
    override val tags: List<String>,
    /** 유지할 기존 첨부 id. 여기 없는 첨부는 지운다. */
    val attachmentIds: List<Long>
) : NoticeReqBody
