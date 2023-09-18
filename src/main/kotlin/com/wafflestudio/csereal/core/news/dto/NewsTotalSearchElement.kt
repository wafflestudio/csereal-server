package com.wafflestudio.csereal.core.news.dto

import com.wafflestudio.csereal.common.utils.substringAroundKeyword
import java.time.LocalDateTime

data class NewsTotalSearchElement private constructor(
        val id: Long,
        val title: String,
        val date: LocalDateTime?,
        val tags: List<String>,
        val imageUrl: String?,
) {
    lateinit var partialDescription: String
    var boldStartIndex: Int = 0
    var boldEndIndex: Int = 0

    constructor(
            id: Long,
            title: String,
            date: LocalDateTime?,
            tags: List<String>,
            imageUrl: String?,
            description: String,
            keyword: String,
            amount: Int,
    ) : this(id, title, date, tags, imageUrl) {
        val (startIdx, substring) = substringAroundKeyword(keyword, description, amount)
        partialDescription = substring
        boldStartIndex = startIdx ?: 0
        boldEndIndex = startIdx?.plus(keyword.length) ?: 0
    }
}
