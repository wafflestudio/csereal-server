package com.wafflestudio.csereal.common.enums

import com.wafflestudio.csereal.common.CserealException

enum class LanguageType {
    KO, EN;

    // TODO: Define custom deserializer, serializer
    companion object {
        fun makeStringToLanguageType(language: String): LanguageType {
            try {
                val upperLanguageType = language.uppercase()
                return LanguageType.valueOf(upperLanguageType)
            } catch (e: IllegalArgumentException) {
                throw CserealException.Csereal400("해당하는 enum을 찾을 수 없습니다")
            }
        }

        // dto로 통신할 때 소문자로 return
        fun makeLowercase(languageType: LanguageType): String = languageType.toString().lowercase()
    }
}
