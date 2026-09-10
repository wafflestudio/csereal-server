package com.wafflestudio.csereal.core.about.database

import com.wafflestudio.csereal.common.sanitize.HtmlContentHolder
import com.wafflestudio.csereal.common.sanitize.HtmlField
import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.utils.StringListConverter
import com.wafflestudio.csereal.common.search.SearchIndexed
import com.wafflestudio.csereal.common.search.SearchType
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
    var locations: MutableList<String> = mutableListOf()
) : BaseTimeEntity(), SearchIndexed, HtmlContentHolder {

    override fun htmlFields() = listOf(HtmlField({ description }, { description = it }))

    override val searchType get() = SearchType.ABOUT
    override val searchSourceId get() = about.id
}
