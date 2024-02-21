package com.wafflestudio.csereal.common.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class StringListConverter : AttributeConverter<MutableList<String>, String> {
    override fun convertToDatabaseColumn(p0: MutableList<String>?): String =
        ObjectMapper().writeValueAsString(p0 ?: mutableListOf<String>())

    override fun convertToEntityAttribute(p0: String?): MutableList<String> =
        ObjectMapper().readValue(p0 ?: "[]")
}
