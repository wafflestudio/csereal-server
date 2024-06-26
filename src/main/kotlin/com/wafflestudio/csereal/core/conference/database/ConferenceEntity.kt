package com.wafflestudio.csereal.core.conference.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.conference.dto.ConferenceDto
import com.wafflestudio.csereal.core.research.database.ResearchSearchEntity
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
    val conferencePage: ConferencePageEntity,

    @OneToOne(mappedBy = "conferenceElement", cascade = [CascadeType.ALL], orphanRemoval = true)
    var researchSearch: ResearchSearchEntity? = null
) : BaseTimeEntity() {
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
