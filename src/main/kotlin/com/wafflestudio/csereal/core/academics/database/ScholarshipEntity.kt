package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.enums.LanguageType
import jakarta.persistence.*

@Entity(name = "scholarship")
class ScholarshipEntity(
    @Enumerated(EnumType.STRING)
    var studentType: AcademicsStudentType,

    @Enumerated(EnumType.STRING)
    var language: LanguageType,

    var name: String,

    @Column(columnDefinition = "text")
    var description: String,

    @OneToOne(mappedBy = "scholarship", cascade = [CascadeType.ALL], orphanRemoval = true)
    var academicsSearch: AcademicsSearchEntity? = null

) : BaseTimeEntity() {

    companion object {
        fun of(
            languageType: LanguageType,
            studentType: AcademicsStudentType,
            name: String,
            description: String
        ): ScholarshipEntity {
            return ScholarshipEntity(
                language = languageType,
                studentType = studentType,
                name = name,
                description = description
            )
        }
    }
}
