package com.wafflestudio.csereal.common.utils

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.mockauth.CustomPrincipal
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.jsoup.safety.Safelist
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.user.OidcUser
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

fun getUsername(authentication: Authentication?): String? {
    val principal = authentication?.principal

    return principal?.let {
        when (principal) {
            is OidcUser -> principal.idToken.getClaim("username")
            is CustomPrincipal -> principal.userEntity.username
            else -> throw CserealException.Csereal401("Unsupported principal type")
        }
    }
}
