package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
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
    val research: ResearchTranslationEntity? = null,

    @OneToOne
    @JoinColumn(name = "lab_id")
    val lab: LabTranslationEntity? = null,

    @OneToOne
    @JoinColumn(name = "conference_id")
    val conferenceElement: ConferenceEntity? = null
) : BaseTimeEntity() {
    companion object {
        fun create(research: ResearchTranslationEntity): ResearchSearchEntity {
            return ResearchSearchEntity(
                content = createContent(research),
                language = research.language,
                research = research
            )
        }

        fun create(lab: LabTranslationEntity): ResearchSearchEntity {
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

        fun createContent(translation: ResearchTranslationEntity) = StringBuilder().apply {
            val research = translation.research
            appendLine(translation.name)
            appendLine(research.postType.krName)
            translation.description?.let {
                appendLine(cleanTextFromHtml(it))
            }
            // 색인은 언어별이므로 딸린 이름도 같은 언어판에서 가져온다.
            research.labs.forEach { lab ->
                lab.translationOf(translation.language)?.let { appendLine(it.name) }
            }
            research.websiteURL?.let { appendLine(it) }
        }.toString()

        fun createContent(translation: LabTranslationEntity) = StringBuilder().apply {
            val lab = translation.lab
            appendLine(translation.name)
            lab.professors.forEach { professor ->
                professor.translationOf(translation.language)?.let { appendLine(it.name) }
            }
            translation.location?.let { appendLine(it) }
            lab.tel?.let { appendLine(it) }
            lab.acronym?.let { appendLine(it) }
            lab.youtube?.let { appendLine(it) }
            lab.research?.translationOf(translation.language)?.let { appendLine(it.name) }
            translation.description?.let {
                appendLine(cleanTextFromHtml(it))
            }
            lab.websiteURL?.let { appendLine(it) }
        }.toString()

        fun createContent(conference: ConferenceEntity) = StringBuilder().apply {
            appendLine(conference.name)
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

    fun update(research: ResearchTranslationEntity) {
        this.content = createContent(research)
    }

    fun update(lab: LabTranslationEntity) {
        this.content = createContent(lab)
    }

    fun update(conference: ConferenceEntity) {
        this.content = createContent(conference)
    }
}

fun ResearchTranslationEntity.syncSearch() {
    researchSearch?.update(this) ?: run { researchSearch = ResearchSearchEntity.create(this) }
}

fun LabTranslationEntity.syncSearch() {
    researchSearch?.update(this) ?: run { researchSearch = ResearchSearchEntity.create(this) }
}

fun ConferenceEntity.syncSearch() {
    researchSearch?.update(this) ?: run { researchSearch = ResearchSearchEntity.create(this) }
}
