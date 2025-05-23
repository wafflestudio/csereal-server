package com.wafflestudio.csereal.common.utils

import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.jsoup.safety.Safelist
import org.springframework.security.core.context.SecurityContextHolder
import kotlin.math.ceil

fun cleanTextFromHtml(description: String): String {
    val cleanDescription = Jsoup.clean(description, Safelist.none())
    return Parser.unescapeEntities(cleanDescription, false)
}

fun substringAroundKeyword(keyword: String, content: String, amount: Int): Pair<Int?, String> {
    val index = content.lowercase().indexOf(keyword.lowercase())
    return if (index == -1) {
        null to content.substring(0, amount.coerceAtMost(content.length))
    } else {
        var frontIndex = (index - amount / 2 + keyword.length).coerceAtLeast(0)
        var backIndex = (index + amount / 2 + keyword.length).coerceAtMost(content.length)

        if (frontIndex == 0) {
            backIndex = (amount).coerceAtMost(content.length)
        } else if (backIndex == content.length) {
            frontIndex = (content.length - amount).coerceAtLeast(0)
        }

        (index - frontIndex) to content.substring(frontIndex, backIndex)
    }
}

fun exchangeValidPageNum(pageSize: Int, pageNum: Int, total: Long): Int {
    // Validate
    if (!(pageSize > 0 && pageNum > 0 && total >= 0)) {
        throw RuntimeException()
    }

    return when {
        total == 0L -> 1
        (pageNum - 1) * pageSize < total -> pageNum
        else -> ceil(total.toDouble() / pageSize).toInt()
    }
}

fun startsWithEnglish(name: String): Boolean {
    return name.isNotEmpty() && name.first().let { it in 'A'..'Z' || it in 'a'..'z' }
}

fun isCurrentUserStaff(): Boolean {
    return "ROLE_STAFF" in getCurrentUserRoles()
}

fun getCurrentUserRoles(): List<String> {
    val authentication = SecurityContextHolder.getContext().authentication ?: /* for test */ return listOf("ROLE_STAFF")
    return authentication.authorities.map { it.authority }
}
