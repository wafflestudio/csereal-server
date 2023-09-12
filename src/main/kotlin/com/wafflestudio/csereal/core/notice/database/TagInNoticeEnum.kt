package com.wafflestudio.csereal.core.notice.database

import com.wafflestudio.csereal.common.CserealException

enum class TagInNoticeEnum {
    CLASS, SCHOLARSHIP, UNDERGRADUATE, GRADUATE, MINOR, REGISTRATIONS, ADMISSIONS, GRADUATIONS,
    RECRUIT, STUDENT_EXCHANGE, INNER_EVENTS_PROGRAMS, OUTER_EVENTS_PROGRAMS, FOREIGN;

    companion object {
        fun getTagEnum(t: String): TagInNoticeEnum {
            return when (t) {
                "수업" -> CLASS
                "장학" -> SCHOLARSHIP
                "학사(학부)" -> UNDERGRADUATE
                "학사(대학원)" -> GRADUATE
                "다전공/전과" -> MINOR
                "등록/복학/휴학/재입학" -> REGISTRATIONS
                "입학" -> ADMISSIONS
                "졸업" -> GRADUATIONS
                "채용정보" -> RECRUIT
                "교환학생/유학" -> STUDENT_EXCHANGE
                "내부행사/프로그램" -> INNER_EVENTS_PROGRAMS
                "외부행사/프로그램" -> OUTER_EVENTS_PROGRAMS
                "foreign" -> FOREIGN
                else -> throw CserealException.Csereal404("태그를 찾을 수 없습니다")
            }
        }
    }


}



