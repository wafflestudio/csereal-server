package com.wafflestudio.csereal.core.about.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.utils.StringListConverter
import com.wafflestudio.csereal.common.utils.cleanTextFromHtml
import jakarta.persistence.*

// locations 는 주소 표기라 언어별로 다르다(["302동 310-2호"] / ["301B 310-2"]).
@Entity(name = "about_translation")
class AboutTranslationEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "about_id")
    var about: AboutEntity,

    @Enumerated(EnumType.STRING)
    var language: LanguageType,

    var name: String?,

    @Column(columnDefinition = "mediumText")
    var description: String,

    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter::class)
    var locations: MutableList<String> = mutableListOf(),

    @Column(columnDefinition = "TEXT")
    var searchContent: String = ""
) : BaseTimeEntity() {

    fun syncSearchContent() {
        assert(about.postType != AboutPostType.FUTURE_CAREERS)
        searchContent = createContent(name, description, locations)
    }

    fun syncSearchContent(statNames: List<String>, companyNames: List<String>) {
        assert(about.postType == AboutPostType.FUTURE_CAREERS)
        searchContent = createContent(name, description, statNames, companyNames)
    }

    companion object {
        fun createContent(name: String?, description: String, locations: List<String>) = StringBuilder().apply {
            name?.let { appendLine(it) }
            appendLine(cleanTextFromHtml(description))
            locations.forEach { appendLine(it) }
        }.toString()

        fun createContent(
            name: String?,
            description: String,
            statNames: List<String>,
            companyNames: List<String>
        ) = StringBuilder().apply {
            name?.let { appendLine(it) }
            appendLine(cleanTextFromHtml(description))
            statNames.forEach { appendLine(it) }
            companyNames.forEach { appendLine(it) }
        }.toString()
    }
}
