package com.wafflestudio.csereal.core.about.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.AttachmentContentEntityType
import com.wafflestudio.csereal.common.controller.MainImageContentEntityType
import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.common.utils.StringListConverter
import com.wafflestudio.csereal.common.utils.cleanTextFromHtml
import com.wafflestudio.csereal.core.about.dto.AboutDto
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import jakarta.persistence.*

@Entity(name = "about")
class AboutEntity(
    @Enumerated(EnumType.STRING)
    var postType: AboutPostType,
    @Enumerated(EnumType.STRING)
    var language: LanguageType = LanguageType.KO,
    var name: String?,

    @Column(columnDefinition = "mediumText")
    var description: String,

    var year: Int?,

    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter::class)
    var locations: MutableList<String> = mutableListOf(),

    @OneToMany(mappedBy = "")
    var attachments: MutableList<AttachmentEntity> = mutableListOf(),

    @OneToOne
    var mainImage: MainImageEntity? = null,

    @Column(columnDefinition = "TEXT")
    var searchContent: String

) : BaseTimeEntity(), MainImageContentEntityType, AttachmentContentEntityType {
    override fun bringMainImage(): MainImageEntity? = mainImage
    override fun bringAttachments(): List<AttachmentEntity> = attachments

    companion object {
        fun of(
            postType: AboutPostType,
            languageType: LanguageType,
            aboutDto: AboutDto
        ): AboutEntity {
            return AboutEntity(
                postType = postType,
                language = languageType,
                name = aboutDto.name,
                description = aboutDto.description,
                year = aboutDto.year,
                locations = aboutDto.locations?.toMutableList() ?: mutableListOf(),
                searchContent = ""
            )
        }

        fun createContent(name: String?, description: String, locations: List<String>) = StringBuilder().apply {
            name?.let { appendLine(it) }
            appendLine(cleanTextFromHtml(description))
            locations.forEach {
                appendLine(it)
            }
        }.toString()

        fun createContent(
            name: String?,
            description: String,
            statNames: List<String>,
            companyNames: List<String>
        ): String {
            return StringBuilder().apply {
                name?.let { appendLine(it) }
                appendLine(cleanTextFromHtml(description))
                statNames.forEach {
                    appendLine(it)
                }
                companyNames.forEach {
                    appendLine(it)
                }
            }.toString()
        }
    }

    fun syncSearchContent() {
        assert(postType != AboutPostType.FUTURE_CAREERS)
        searchContent = createContent(name, description, locations)
    }

    fun syncSearchContent(statNames: List<String>, companyNames: List<String>) {
        assert(postType == AboutPostType.FUTURE_CAREERS)
        searchContent = createContent(name, description, statNames, companyNames)
    }
}
