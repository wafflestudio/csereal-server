package com.wafflestudio.csereal.common.repository

import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.NumberTemplate
import org.springframework.stereotype.Repository

interface CommonRepository {
    fun searchFullSingleTextTemplate(keyword: String, field: Any): NumberTemplate<Double>
    fun searchFullDoubleTextTemplate(keyword: String, field1: Any, field2: Any): NumberTemplate<Double>
    fun searchFullTripleTextTemplate(keyword: String, field1: Any, field2: Any, field3: Any): NumberTemplate<Double>
    fun searchFullQuadrapleTextTemplate(
        keyword: String,
        field1: Any,
        field2: Any,
        field3: Any,
        field4: Any
    ): NumberTemplate<Double>

    fun searchFullQuintupleTextTemplate(
        keyword: String,
        field1: Any,
        field2: Any,
        field3: Any,
        field4: Any,
        field5: Any
    ): NumberTemplate<Double>

    fun searchFullSextupleTextTemplate(
        keyword: String,
        field1: Any,
        field2: Any,
        field3: Any,
        field4: Any,
        field5: Any,
        field6: Any
    ): NumberTemplate<Double>

    fun searchFullSeptupleTextTemplate(
        keyword: String,
        field1: Any,
        field2: Any,
        field3: Any,
        field4: Any,
        field5: Any,
        field6: Any,
        field7: Any
    ): NumberTemplate<Double>

    fun searchFullOctupleTextTemplate(
        keyword: String,
        field1: Any,
        field2: Any,
        field3: Any,
        field4: Any,
        field5: Any,
        field6: Any,
        field7: Any,
        field8: Any
    ): NumberTemplate<Double>
}

@Repository
class CommonRepositoryImpl : CommonRepository {
    override fun searchFullSingleTextTemplate(
        keyword: String,
        field: Any
    ) = Expressions.numberTemplate(
        Double::class.javaObjectType,
        "function('match',{0},{1})",
        field,
        replaceOperatorsToSpace(keyword)
    )

    override fun searchFullDoubleTextTemplate(
        keyword: String,
        field1: Any,
        field2: Any
    ) = Expressions.numberTemplate(
        Double::class.javaObjectType,
        "function('match2',{0},{1},{2})",
        field1,
        field2,
        replaceOperatorsToSpace(keyword)
    )

    override fun searchFullTripleTextTemplate(
        keyword: String,
        field1: Any,
        field2: Any,
        field3: Any
    ) = Expressions.numberTemplate(
        Double::class.javaObjectType,
        "function('match3',{0},{1},{2},{3})",
        field1,
        field2,
        field3,
        replaceOperatorsToSpace(keyword)
    )

    override fun searchFullQuadrapleTextTemplate(
        keyword: String,
        field1: Any,
        field2: Any,
        field3: Any,
        field4: Any
    ) = Expressions.numberTemplate(
        Double::class.javaObjectType,
        "function('match4',{0},{1},{2},{3},{4})",
        field1,
        field2,
        field3,
        field4,
        replaceOperatorsToSpace(keyword)
    )

    override fun searchFullQuintupleTextTemplate(
        keyword: String,
        field1: Any,
        field2: Any,
        field3: Any,
        field4: Any,
        field5: Any
    ) = Expressions.numberTemplate(
        Double::class.javaObjectType,
        "function('match5',{0},{1},{2},{3},{4},{5})",
        field1,
        field2,
        field3,
        field4,
        field5,
        replaceOperatorsToSpace(keyword)
    )

    override fun searchFullSextupleTextTemplate(
        keyword: String,
        field1: Any,
        field2: Any,
        field3: Any,
        field4: Any,
        field5: Any,
        field6: Any
    ) = Expressions.numberTemplate(
        Double::class.javaObjectType,
        "function('match6',{0},{1},{2},{3},{4},{5},{6})",
        field1,
        field2,
        field3,
        field4,
        field5,
        field6,
        replaceOperatorsToSpace(keyword)
    )

    override fun searchFullSeptupleTextTemplate(
        keyword: String,
        field1: Any,
        field2: Any,
        field3: Any,
        field4: Any,
        field5: Any,
        field6: Any,
        field7: Any
    ) = Expressions.numberTemplate(
        Double::class.javaObjectType,
        "function('match7',{0},{1},{2},{3},{4},{5},{6},{7})",
        field1,
        field2,
        field3,
        field4,
        field5,
        field6,
        field7,
        replaceOperatorsToSpace(keyword)
    )

    override fun searchFullOctupleTextTemplate(
        keyword: String,
        field1: Any,
        field2: Any,
        field3: Any,
        field4: Any,
        field5: Any,
        field6: Any,
        field7: Any,
        field8: Any
    ) = Expressions.numberTemplate(
        Double::class.javaObjectType,
        "function('match8',{0},{1},{2},{3},{4},{5},{6},{7},{8})",
        field1,
        field2,
        field3,
        field4,
        field5,
        field6,
        field7,
        field8,
        replaceOperatorsToSpace(keyword)
    )

    val operatorRegex = """[+\-<>'"@()~*]""".toRegex()

    // Currently, we do not want user to search with operators, or having sql error by using @ operator.
    // So we replace all operators to space.
    // This should be changed if we want to support operators or advanced search in the future.
    private inline fun replaceOperatorsToSpace(keyword: String): String =
        operatorRegex.replace(keyword, " ")
}
