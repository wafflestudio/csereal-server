package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.utils.cleanTextFromHtml
import com.wafflestudio.csereal.core.conference.database.ConferenceEntity
import jakarta.persistence.*

@Entity(name = "research_search")
class ResearchSearchEntity(
    @Column(columnDefinition = "TEXT")
    var content: String,

    @Enumerated(value = EnumType.STRING)
    val language: LanguageType,

    @OneToOne
    @JoinColumn(name = "research_id")
    val research: ResearchEntity? = null,

    @OneToOne
    @JoinColumn(name = "lab_id")
    val lab: LabEntity? = null,

    @OneToOne
    @JoinColumn(name = "conference_id")
    val conferenceElement: ConferenceEntity? = null
) : BaseTimeEntity() {
    companion object {
        fun create(research: ResearchEntity): ResearchSearchEntity {
            return ResearchSearchEntity(
                content = createContent(research),
                language = research.language,
                research = research
            )
        }

        fun create(lab: LabEntity): ResearchSearchEntity {
            return ResearchSearchEntity(
                content = createContent(lab),
                language = lab.language,
                lab = lab
            )
        }

        fun create(conference: ConferenceEntity): ResearchSearchEntity {
            return ResearchSearchEntity(
                content = createContent(conference),
                language = conference.language,
                conferenceElement = conference
            )
        }

        fun createContent(research: ResearchEntity) = StringBuilder().apply {
            appendLine(research.name)
            appendLine(research.postType.krName)
            research.description?.let {
                appendLine(cleanTextFromHtml(it))
            }
            research.labs.forEach { appendLine(it.name) }
            research.websiteURL?.let { appendLine(it) }
        }.toString()

        fun createContent(lab: LabEntity) = StringBuilder().apply {
            appendLine(lab.name)
            lab.professors.forEach { appendLine(it.name) }
            lab.location?.let { appendLine(it) }
            lab.tel?.let { appendLine(it) }
            lab.acronym?.let { appendLine(it) }
            lab.youtube?.let { appendLine(it) }
            appendLine(lab.research.name)
            lab.description?.let {
                appendLine(cleanTextFromHtml(it))
            }
            lab.websiteURL?.let { appendLine(it) }
        }.toString()

        fun createContent(conference: ConferenceEntity) = StringBuilder().apply {
            appendLine(conference.name)
            appendLine(conference.code)
            appendLine(conference.abbreviation)
        }.toString()
    }

    @PrePersist
    @PreUpdate
    fun checkType() {
        if (!(
            (research != null && lab == null && conferenceElement == null) ||
                (research == null && lab != null && conferenceElement == null) ||
                (research == null && lab == null && conferenceElement != null)
            )
        ) {
            throw RuntimeException("ResearchSearchEntity must have either research or lab or conference")
        }
    }

    fun update(research: ResearchEntity) {
        this.content = createContent(research)
    }

    fun update(lab: LabEntity) {
        this.content = createContent(lab)
    }

    fun update(conference: ConferenceEntity) {
        this.content = createContent(conference)
    }
}

enum class ResearchSearchType {
    RESEARCH_GROUP,
    RESEARCH_CENTER,
    LAB,
    CONFERENCE;
}
