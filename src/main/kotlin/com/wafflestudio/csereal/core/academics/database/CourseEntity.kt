package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.sanitize.HtmlContentHolder
import com.wafflestudio.csereal.common.sanitize.HtmlField
import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.search.SearchIndexed
import com.wafflestudio.csereal.common.search.SearchType
import com.wafflestudio.csereal.common.enums.LanguageType
import jakarta.persistence.*

@Entity(name = "course")
class CourseEntity(
    @Enumerated(EnumType.STRING)
    var studentType: AcademicsStudentType,

    @Enumerated(EnumType.STRING)
    var language: LanguageType,

    var classification: String,
    var code: String,
    var name: String,
    var credit: Int,
    var grade: Int,

    @Column(columnDefinition = "mediumText")
    var description: String?

) : BaseTimeEntity(), SearchIndexed, HtmlContentHolder {

    override fun htmlFields() = listOf(HtmlField({ description }, { description = it }))

    override val searchType get() = SearchType.COURSE
    override val searchSourceId get() = id

    companion object {
        fun of(
            studentType: AcademicsStudentType,
            languageType: LanguageType,
            classification: String,
            code: String,
            name: String,
            credit: Int,
            grade: Int,
            description: String?
        ): CourseEntity {
            return CourseEntity(
                studentType = studentType,
                language = languageType,
                classification = classification,
                code = code,
                name = name,
                credit = credit,
                grade = grade,
                description = description
            )
        }
    }
}
