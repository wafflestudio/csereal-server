package com.wafflestudio.csereal.common.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Configuration
class JacksonConfig(private val objectMapper: ObjectMapper) {

    @EventListener(ApplicationReadyEvent::class)
    fun setUp() {
        val module = SimpleModule()
        module.addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer())
        objectMapper.registerModule(module)
    }
}

class LocalDateTimeSerializer : JsonSerializer<LocalDateTime>() {
    override fun serialize(value: LocalDateTime, gen: JsonGenerator, serializers: SerializerProvider) {
        val zonedDateTime = value.atZone(ZoneOffset.UTC)
        val formatted = zonedDateTime.format(DateTimeFormatter.ISO_INSTANT)
        gen.writeString(formatted)
    }
}
