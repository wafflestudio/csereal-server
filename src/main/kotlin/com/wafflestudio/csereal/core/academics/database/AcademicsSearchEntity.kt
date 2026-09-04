package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.utils.cleanTextFromHtml
import jakarta.persistence.*

@Entity(name = "academics_search")
class AcademicsSearchEntity(
    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    @Enumerated(value = EnumType.STRING)
    val language: LanguageType,

    @OneToOne
    @JoinColumn(name = "academics_id")
    val academics: AcademicsEntity? = null,

    @OneToOne
    @JoinColumn(name = "course_id")
    val course: CourseEntity? = null,

    // 색인은 언어별이라 장학금 자체가 아니라 번역본에 붙는다.
    @OneToOne
    @JoinColumn(name = "scholarship_id")
    val scholarship: ScholarshipTranslationEntity? = null

) : BaseTimeEntity() {
    companion object {
        fun create(academics: AcademicsEntity): AcademicsSearchEntity {
            return AcademicsSearchEntity(
                academics = academics,
                language = academics.language,
                content = createContent(academics)
            )
        }

        fun create(course: CourseEntity): AcademicsSearchEntity {
            return AcademicsSearchEntity(
                course = course,
                language = course.language,
                content = createContent(course)
            )
        }

        fun create(scholarship: ScholarshipTranslationEntity): AcademicsSearchEntity {
            return AcademicsSearchEntity(
                scholarship = scholarship,
                language = scholarship.language,
                content = createContent(scholarship)
            )
        }

        fun createContent(academics: AcademicsEntity): String {
            val sb = StringBuilder()
            academics.name.let { sb.appendLine(it) }
            academics.year?.let { sb.appendLine(it) }
            sb.appendLine(academics.studentType.value)
            sb.appendLine(
                cleanTextFromHtml(
                    academics.description
                )
            )

            return sb.toString()
        }

        fun createContent(course: CourseEntity) =
            course.let {
                val sb = StringBuilder()
                sb.appendLine(it.studentType.value)
                sb.appendLine(it.classification)
                sb.appendLine(it.code)
                sb.appendLine(it.name)
                sb.appendLine(it.credit)
                sb.appendLine(it.grade)
                it.description?.let { desc ->
                    sb.appendLine(cleanTextFromHtml(desc))
                }

                sb.toString()
            }

        fun createContent(scholarship: ScholarshipTranslationEntity) =
            scholarship.let {
                val sb = StringBuilder()
                sb.appendLine(it.scholarship.studentType.value)
                sb.appendLine(it.name)
                sb.appendLine(
                    cleanTextFromHtml(it.description)
                )
                sb.toString()
            }
    }

    fun update(academics: AcademicsEntity) {
        this.content = createContent(academics)
    }

    fun update(course: CourseEntity) {
        this.content = createContent(course)
    }

    fun update(scholarship: ScholarshipTranslationEntity) {
        this.content = createContent(scholarship)
    }

    @PrePersist
    @PreUpdate
    fun checkType() {
        if (!(
            (academics != null && course == null && scholarship == null) ||
                (academics == null && course != null && scholarship == null) ||
                (academics == null && course == null && scholarship != null)
            )
        ) {
            throw IllegalStateException("AcademicsSearchEntity must have only one type of entity")
        }
    }

    fun ofType() =
        when {
            academics != null && course == null && scholarship == null -> AcademicsSearchType.ACADEMICS
            academics == null && course != null && scholarship == null -> AcademicsSearchType.COURSE
            academics == null && course == null && scholarship != null -> AcademicsSearchType.SCHOLARSHIP
            else -> throw IllegalStateException("AcademicsSearchEntity must have only one type of entity")
        }
}

enum class AcademicsSearchType {
    ACADEMICS,
    COURSE,
    SCHOLARSHIP;

    @com.fasterxml.jackson.annotation.JsonValue
    fun toValue() = name.lowercase().replace('_', '-')
}
