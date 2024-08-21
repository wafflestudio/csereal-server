package com.wafflestudio.csereal.common.aop

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.wafflestudio.csereal.common.config.EnumCaseSeperatorChangeDeserializer
import com.wafflestudio.csereal.common.config.EnumCaseSeperatorChangeSerializer

@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.FIELD,
    AnnotationTarget.CLASS,
    AnnotationTarget.TYPE_PARAMETER,
    AnnotationTarget.VALUE_PARAMETER,
)
@Retention(AnnotationRetention.RUNTIME)
@JsonSerialize(using = EnumCaseSeperatorChangeSerializer::class)
@JsonDeserialize(using = EnumCaseSeperatorChangeDeserializer::class)
annotation class JsonEnumConversion
