package com.wafflestudio.csereal.core.conference.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity(name = "conference")
class ConferenceEntity(
    val code: String,
    val abbreviation: String,
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conference_page_id")
    val conferencePage: ConferencePageEntity

) : BaseTimeEntity()
