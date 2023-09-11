package com.wafflestudio.csereal.common.utils

import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.jsoup.safety.Safelist

fun cleanTextFromHtml(description: String): String {
    val cleanDescription = Jsoup.clean(description, Safelist.none())
    return Parser.unescapeEntities(cleanDescription, false)
}
