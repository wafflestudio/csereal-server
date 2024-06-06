package com.wafflestudio.csereal.core.notice.database

import com.wafflestudio.csereal.common.CserealException

enum class TagInNoticeEnum(val krName: String) {
    CLASS("수업"), SCHOLARSHIP("장학"), UNDERGRADUATE("학사(학부)"), GRADUATE("학사(대학원)"),
    MINOR("다전공/전과"), REGISTRATIONS("등록/복학/휴학/재입학"), ADMISSIONS("입학"), GRADUATIONS("졸업"),
    RECRUIT("채용정보"), STUDENT_EXCHANGE("교환학생/유학"), INNER_EVENTS_PROGRAMS("내부행사/프로그램"),
    OUTER_EVENTS_PROGRAMS("외부행사/프로그램"), FOREIGN("foreign");

    companion object {
        private val lookupMap: Map<String, TagInNoticeEnum> = entries.associateBy(TagInNoticeEnum::krName)

        fun getTagEnum(t: String): TagInNoticeEnum {
            return lookupMap[t] ?: throw CserealException.Csereal404("태그를 찾을 수 없습니다: $t")
        }
    }
}
