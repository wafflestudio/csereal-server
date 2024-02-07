package com.wafflestudio.csereal.common.repository

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.properties.LanguageType
import org.springframework.stereotype.Repository

interface LanguageRepository {
    fun makeStringToLanguageType(language: String): LanguageType
}

@Repository
class LanguageRepositoryImpl : LanguageRepository {
    override fun makeStringToLanguageType(language: String): LanguageType {
        try {
            val upperLanguageType = language.uppercase()
            return LanguageType.valueOf(upperLanguageType)
        } catch (e: IllegalArgumentException) {
            throw CserealException.Csereal400("해당하는 enum을 찾을 수 없습니다")
        }
    }
}
