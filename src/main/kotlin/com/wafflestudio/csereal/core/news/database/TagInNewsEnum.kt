package com.wafflestudio.csereal.core.news.database

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode

enum class TagInNewsEnum(val krName: String) {
    EVENT("행사"), RESEARCH("연구"), AWARDS("수상"), RECRUIT("채용"), COLUMN("칼럼"),
    LECTURE("강연"), EDUCATION("교육"), INTERVIEW("인터뷰"), CAREER("진로"), UNCLASSIFIED("과거 미분류");

    companion object {
        private val lookupMap: Map<String, TagInNewsEnum> = TagInNewsEnum.values().associateBy(TagInNewsEnum::krName)

        fun getTagEnum(t: String): TagInNewsEnum {
            return lookupMap[t] ?: throw CserealException(ErrorCode.NEWS_TAG_NOT_FOUND, mapOf("t" to t))
        }
    }
}
