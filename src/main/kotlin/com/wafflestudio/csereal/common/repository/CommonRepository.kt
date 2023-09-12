package com.wafflestudio.csereal.common.repository

import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.NumberTemplate
import org.springframework.stereotype.Repository

interface CommonRepository {
    fun searchFullSingleTextTemplate(keyword: String, field: Any): NumberTemplate<Double>
    fun searchFullDoubleTextTemplate(keyword: String, field1: Any, field2: Any): NumberTemplate<Double>
    fun searchFullTripleTextTemplate(keyword: String, field1: Any, field2: Any, field3: Any): NumberTemplate<Double>
    fun searchFullQuadrapleTextTemplate(keyword: String, field1: Any, field2: Any, field3: Any, field4: Any): NumberTemplate<Double>
}

@Repository
class CommonRepositoryImpl: CommonRepository {
    override fun searchFullSingleTextTemplate(
            keyword: String,
            field: Any,
    ) = Expressions.numberTemplate(
            Double::class.javaObjectType,
            "function('match',{0},{1})",
            field,
            keyword
        )

    override fun searchFullDoubleTextTemplate(
            keyword: String,
            field1: Any,
            field2: Any,
    ) = Expressions.numberTemplate(
            Double::class.javaObjectType,
            "function('match2',{0},{1},{2})",
            field1,
            field2,
            keyword,
    )

    override fun searchFullTripleTextTemplate(
            keyword: String,
            field1: Any,
            field2: Any,
            field3: Any,
    ) = Expressions.numberTemplate(
            Double::class.javaObjectType,
            "function('match3',{0},{1},{2},{3})",
            field1,
            field2,
            field3,
            keyword
    )

    override fun searchFullQuadrapleTextTemplate(
            keyword: String,
            field1: Any,
            field2: Any,
            field3: Any,
            field4: Any,
    ) = Expressions.numberTemplate(
            Double::class.javaObjectType,
            "function('match3',{0},{1},{2},{3},{4})",
            field1,
            field2,
            field3,
            field4,
            keyword
    )
}