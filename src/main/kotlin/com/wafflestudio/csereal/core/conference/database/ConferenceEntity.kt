package com.wafflestudio.csereal.core.conference.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.search.SearchIndexed
import com.wafflestudio.csereal.common.search.SearchType
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.conference.dto.ConferenceDto
import jakarta.persistence.*

@Entity(name = "conference")
class ConferenceEntity(
    @Enumerated(EnumType.STRING)
    var language: LanguageType,

    var isDeleted: Boolean = false,
    var abbreviation: String,
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conference_page_id")
    val conferencePage: ConferencePageEntity

) : BaseTimeEntity(), SearchIndexed {

    override val searchType get() = SearchType.CONFERENCE
    override val searchSourceId get() = id

    companion object {
        fun of(
            languageType: LanguageType,
            conferenceDto: ConferenceDto,
            conferencePage: ConferencePageEntity
        ) = ConferenceEntity(
            language = languageType,
            abbreviation = conferenceDto.abbreviation,
            name = conferenceDto.name,
            conferencePage = conferencePage
        )
    }

    fun update(conferenceDto: ConferenceDto) {
        this.abbreviation = conferenceDto.abbreviation
        this.name = conferenceDto.name
    }
}
