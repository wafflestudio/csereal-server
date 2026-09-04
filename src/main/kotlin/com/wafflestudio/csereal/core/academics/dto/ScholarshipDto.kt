package com.wafflestudio.csereal.core.academics.dto

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.academics.database.AcademicsStudentType
import com.wafflestudio.csereal.core.academics.database.ScholarshipEntity
import com.wafflestudio.csereal.core.academics.database.ScholarshipTranslationEntity

// 요청 본문과 같은 모양. 언어와 무관한 값은 최상위, 언어별 값만 ko/en 안에.
data class ScholarshipLanguagesDto(
    val id: Long,
    val studentType: AcademicsStudentType,
    val ko: ScholarshipTranslationDto?,
    val en: ScholarshipTranslationDto?
) {
    companion object {
        fun of(scholarship: ScholarshipEntity) = ScholarshipLanguagesDto(
            id = scholarship.id,
            studentType = scholarship.studentType,
            ko = scholarship.translationOf(LanguageType.KO)?.let { ScholarshipTranslationDto.of(it) },
            en = scholarship.translationOf(LanguageType.EN)?.let { ScholarshipTranslationDto.of(it) }
        )
    }
}

data class ScholarshipTranslationDto(
    val name: String,
    val description: String
) {
    companion object {
        fun of(translation: ScholarshipTranslationEntity) = ScholarshipTranslationDto(
            name = translation.name,
            description = translation.description
        )
    }
}
