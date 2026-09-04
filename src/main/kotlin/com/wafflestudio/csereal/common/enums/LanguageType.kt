package com.wafflestudio.csereal.common.enums

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode

enum class LanguageType {
    KO, EN;

    // TODO: Define custom deserializer, serializer
    companion object {
        fun makeStringToLanguageType(language: String): LanguageType {
            try {
                val upperLanguageType = language.uppercase()
                return LanguageType.valueOf(upperLanguageType)
            } catch (e: IllegalArgumentException) {
                throw CserealException(ErrorCode.INVALID_ENUM_VALUE)
            }
        }

        // dto로 통신할 때 소문자로 return
        fun makeLowercase(languageType: LanguageType): String = languageType.toString().lowercase()
    }

    @com.fasterxml.jackson.annotation.JsonValue
    fun toValue() = name.lowercase().replace('_', '-')
}
