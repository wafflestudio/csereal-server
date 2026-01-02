package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
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
    var description: String?,

    @OneToOne(mappedBy = "course", cascade = [CascadeType.ALL], orphanRemoval = true)
    var academicsSearch: AcademicsSearchEntity? = null

) : BaseTimeEntity() {

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
