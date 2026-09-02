package com.wafflestudio.csereal.common.config

import org.springframework.core.convert.converter.Converter
import org.springframework.core.convert.converter.ConverterFactory
import org.springframework.stereotype.Component

/**
 * URL 경로·쿼리 파라미터의 문자열을 enum으로 변환한다.
 * (프론트는 `ko`·`future-careers`로 보내고 enum은 `KO`·`FUTURE_CAREERS`).
 */
@Component
class StringToEnumConverterFactory : ConverterFactory<String, Enum<*>> {
    override fun <T : Enum<*>> getConverter(targetType: Class<T>): Converter<String, T> =
        Converter { source ->
            val normalized = source.trim().replace('-', '_').uppercase()
            targetType.enumConstants.firstOrNull { it.name == normalized }
                ?: throw IllegalArgumentException("$source is not a valid ${targetType.simpleName}")
        }
}
