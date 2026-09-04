package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.core.academics.database.ScholarshipTranslationEntity

data class SimpleScholarshipDto(
    val id: Long,
    val name: String
) {
    companion object {
        // id 는 장학금 자체(부모)의 id 다 — 상세 조회가 그 id 로 한/영을 함께 돌려준다.
        fun of(translation: ScholarshipTranslationEntity): SimpleScholarshipDto = SimpleScholarshipDto(
            id = translation.scholarship.id,
            name = translation.name
        )
    }
}
