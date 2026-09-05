package com.wafflestudio.csereal.common.search

import com.fasterxml.jackson.annotation.JsonValue

/**
 * 색인 문서의 종류. 기준은 하나다 — **id 공간 하나에 타입 하나.**
 * 그래야 `type:sourceId` 가 정의상 유일해지고, domain + subtype 처럼 같은 정보를
 * 두 벌로 들고 있을 일이 없다.
 *
 * 화면의 묶음(구성원 = 교수 + 직원)은 검색 응답을 만들 때 코드가 접는다. 색인에는 안 들어간다.
 */
enum class SearchType {
    NOTICE, NEWS, SEMINAR, ABOUT, ADMISSIONS,
    PROFESSOR, STAFF,
    RESEARCH, LAB, CONFERENCE,
    ACADEMICS, COURSE, SCHOLARSHIP;

    @JsonValue
    fun toValue() = name.lowercase()
}
