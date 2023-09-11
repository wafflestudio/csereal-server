package com.wafflestudio.csereal.core.conference.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity(name = "conference")
class ConferenceEntity(
        var isDeleted: Boolean = false,
        var code: String,
        var abbreviation: String,
        var name: String,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "conference_page_id")
        val conferencePage: ConferencePageEntity,

        @OneToOne(mappedBy = "conferenceElement", cascade = [CascadeType.ALL], orphanRemoval = true)
        var researchSearch: ResearchSearchEntity? = null,
) : BaseTimeEntity() {
}

) : BaseTimeEntity()
