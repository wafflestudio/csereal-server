package com.wafflestudio.csereal.core.admissions.type

import com.wafflestudio.csereal.common.enums.LanguageType

enum class AdmissionsMainType(
    val ko: String,
    val en: String
) {
    UNDERGRADUATE("학부", "Undergraduate"),
    GRADUATE("대학원", "Graduate"),
    INTERNATIONAL("International", "International");

    fun getLanguageValue(language: LanguageType) = when (language) {
        LanguageType.KO -> this.ko
        LanguageType.EN -> this.en
    }

    @com.fasterxml.jackson.annotation.JsonValue
    fun toValue() = name.lowercase().replace('_', '-')
}
