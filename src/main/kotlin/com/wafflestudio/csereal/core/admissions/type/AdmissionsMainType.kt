package com.wafflestudio.csereal.core.admissions.type

import com.wafflestudio.csereal.common.CserealException
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

    fun toJsonValue() = this.name.lowercase()

    companion object {
        fun fromJsonValue(field: String) = try {
            field
                .uppercase()
                .let { AdmissionsMainType.valueOf(it) }
        } catch (e: IllegalArgumentException) {
            throw CserealException.Csereal400("존재하지 않는 Admission Main Type입니다.")
        }
    }
}
