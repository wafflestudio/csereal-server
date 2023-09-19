package com.wafflestudio.csereal.core.notice.dto

import com.wafflestudio.csereal.common.utils.substringAroundKeyword
import java.time.LocalDateTime

data class NoticeTotalSearchElement private constructor(
    val id: Long,
    val title: String,
    val createdAt: LocalDateTime
) {
    lateinit var partialDescription: String
    var boldStartIndex: Int = 0
    var boldEndIndex: Int = 0

    constructor(
        id: Long,
        title: String,
        createdAt: LocalDateTime,
        description: String,
        keyword: String,
        amount: Int
    ) : this(id, title, createdAt) {
        val (startIdx, partialDescription) = substringAroundKeyword(keyword, description, amount)
        this.boldStartIndex = startIdx ?: 0
        this.boldEndIndex = startIdx ?. let { it + keyword.length } ?: 0
        this.partialDescription = partialDescription
    }
}
