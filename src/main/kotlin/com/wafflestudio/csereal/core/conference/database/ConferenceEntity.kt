package com.wafflestudio.csereal.core.conference.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.research.database.ResearchSearchEntity
import jakarta.persistence.*

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
