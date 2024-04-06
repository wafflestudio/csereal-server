package com.wafflestudio.csereal.common.enums

import com.wafflestudio.csereal.common.CserealException

enum class ContentSearchSortType {
    DATE,
    RELEVANCE;

    companion object {
        fun fromJsonValue(field: String) =
            try {
                field.replace('-', '_')
                    .uppercase()
                    .let { ContentSearchSortType.valueOf(it) }
            } catch (e: IllegalArgumentException) {
                throw CserealException.Csereal400("잘못된 Sort Type이 주어졌습니다.")
            }
    }
}
