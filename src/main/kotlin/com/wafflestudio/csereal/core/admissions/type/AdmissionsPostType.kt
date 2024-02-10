package com.wafflestudio.csereal.core.admissions.type

import com.wafflestudio.csereal.common.CserealException

enum class AdmissionsPostType {
    // For graduate, undergraduate
    EARLY_ADMISSION,
    REGULAR_ADMISSION,

    // For international
    UNDERGRADUATE,
    GRADUATE,
    EXCHANGE_VISITING,
    SCHOLARSHIPS;

    fun toJsonValue() = this.name.lowercase()

    companion object {
        fun fromJsonValue(field: String) =
            try {
                field.replace('-', '_')
                    .uppercase()
                    .let { AdmissionsPostType.valueOf(it) }
            } catch (e: IllegalArgumentException) {
                throw CserealException.Csereal400("잘못된 Admission Post Type이 주어졌습니다.")
            }
    }
}
