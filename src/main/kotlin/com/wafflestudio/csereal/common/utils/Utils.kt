package com.wafflestudio.csereal.common.utils

import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.jsoup.safety.Safelist
import kotlin.math.max
import kotlin.math.min

fun cleanTextFromHtml(description: String): String {
    val cleanDescription = Jsoup.clean(description, Safelist.none())
    return Parser.unescapeEntities(cleanDescription, false)
}

fun substringAroundKeyword(keyword: String, content: String, amount: Int): Pair<Int?, String> {
    val index = content.indexOf(keyword)
    return if (index == -1) {
         null to content.substring(0, amount.coerceAtMost(content.length))
    } else {
        var frontIndex = (index - amount / 2).coerceAtLeast(0)
        var backIndex = (index + amount / 2).coerceAtMost(content.length)

        if (frontIndex == 0) {
            backIndex = (amount).coerceAtMost(content.length)
        } else if (backIndex == content.length) {
            frontIndex = (content.length - amount).coerceAtLeast(0)
        }

        (index - frontIndex) to content.substring(frontIndex, backIndex)
    }
}
