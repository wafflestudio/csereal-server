package com.wafflestudio.csereal.core.admissions.type

import com.wafflestudio.csereal.common.CserealException

enum class AdmissionsMainType {
    UNDERGRADUATE,
    GRADUATE,
    INTERNATIONAL;

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
