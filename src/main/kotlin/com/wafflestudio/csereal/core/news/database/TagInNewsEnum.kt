package com.wafflestudio.csereal.core.news.database

import com.wafflestudio.csereal.common.CserealException

enum class TagInNewsEnum {
    EVENT, RESEARCH, AWARDS, RECRUIT, COLUMN, LECTURE, EDUCATION, INTERVIEW, CAREER, UNCLASSIFIED;

    companion object {
        fun getTagEnum(t: String) : TagInNewsEnum {
            return when (t) {
                "행사" -> EVENT
                "연구" -> RESEARCH
                "수상" -> AWARDS
                "채용" -> RECRUIT
                "칼럼" -> COLUMN
                "강연" -> LECTURE
                "교육" -> EDUCATION
                "인터뷰" -> INTERVIEW
                "진로" -> CAREER
                "과거 미분류" -> UNCLASSIFIED
                else -> throw CserealException.Csereal404("태그를 찾을 수 없습니다")
            }
        }

        fun getTagString(t: TagInNewsEnum): String {
            return when (t) {
                EVENT -> "행사"
                RESEARCH -> "연구"
                AWARDS -> "수상"
                RECRUIT -> "채용"
                COLUMN -> "칼럼"
                LECTURE -> "강연"
                EDUCATION -> "교육"
                INTERVIEW -> "인터뷰"
                CAREER -> "진로"
                UNCLASSIFIED -> "과거 미분류"
            }
        }
    }
}