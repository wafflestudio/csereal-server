package com.wafflestudio.csereal.common.properties

import com.wafflestudio.csereal.common.CserealException

enum class LanguageType {
    KO, EN;

    companion object {
        fun makeStringToLanguageType(language: String?): LanguageType {
            try {
                if (language.isNullOrEmpty()) {
                    return KO
                }
                val upperLanguageType = language.uppercase()
                return LanguageType.valueOf(upperLanguageType)
            } catch (e: IllegalArgumentException) {
                throw CserealException.Csereal400("해당하는 enum을 찾을 수 없습니다")
            }
        }
    }
}
