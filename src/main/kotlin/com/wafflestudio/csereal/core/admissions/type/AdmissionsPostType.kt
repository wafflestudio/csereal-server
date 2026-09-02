package com.wafflestudio.csereal.core.admissions.type

import com.wafflestudio.csereal.common.enums.LanguageType

enum class AdmissionsPostType(
    val ko: String,
    val en: String
) {
    // For graduate, undergraduate
    EARLY_ADMISSION("수시 모집", "Early Admission"),
    REGULAR_ADMISSION("정시 모집", "Regular Admission"),

    // For international
    UNDERGRADUATE("Undergraduate", "Undergraduate"),
    GRADUATE("Graduate", "Graduate"),
    EXCHANGE_VISITING("Exchange/Visiting Program", "Exchange/Visiting Program"),
    SCHOLARSHIPS("Scholarships", "Scholarships") ;

    fun getLanguageValue(language: LanguageType) = when (language) {
        LanguageType.KO -> this.ko
        LanguageType.EN -> this.en
    }

    @com.fasterxml.jackson.annotation.JsonValue
    fun toValue() = name.lowercase().replace('_', '-')
}
