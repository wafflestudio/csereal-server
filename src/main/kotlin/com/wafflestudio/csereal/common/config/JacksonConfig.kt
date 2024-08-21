package com.wafflestudio.csereal.common.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
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

// TODO: Apply to enums
class EnumCaseSeperatorChangeDeserializer<T : Enum<T>>(vc: Class<T>?) : StdDeserializer<T>(vc), ContextualDeserializer {
    constructor() : this(null)

    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ) = (this._valueClass as Class<T>).enumConstants.find {
        p.valueAsString
            ?.uppercase()
            ?.replace('-', '_')
            ?.let { s -> s == it.name }
            ?: false
    }

    override fun createContextual(ctxt: DeserializationContext, property: BeanProperty): JsonDeserializer<*> =
        EnumCaseSeperatorChangeDeserializer((property.type.rawClass) as Class<T>)
}

// TODO: Apply to enums
class EnumCaseSeperatorChangeSerializer<T : Enum<T>>(vc: Class<T>?) : StdSerializer<T>(vc), ContextualSerializer {
    constructor() : this(null)

    override fun serialize(value: T, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(
            value.name.lowercase().replace('_', '-')
        )
    }

    override fun createContextual(prov: SerializerProvider, property: BeanProperty): JsonSerializer<*> =
        EnumCaseSeperatorChangeSerializer((property.type.rawClass) as Class<T>)
}
