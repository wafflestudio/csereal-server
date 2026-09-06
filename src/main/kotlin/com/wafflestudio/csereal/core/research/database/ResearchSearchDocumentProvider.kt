package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import com.wafflestudio.csereal.common.search.SearchDocument
import com.wafflestudio.csereal.common.search.SearchDocumentProvider
import com.wafflestudio.csereal.common.search.SearchType
import com.wafflestudio.csereal.core.conference.database.ConferenceRepository
import com.wafflestudio.csereal.core.research.type.ResearchType
import org.springframework.stereotype.Component
import com.wafflestudio.csereal.core.conference.database.ConferenceEntity
import com.wafflestudio.csereal.common.utils.cleanTextFromHtml

@Component
class ResearchSearchDocumentProvider(
    private val researchRepository: ResearchRepository,
    private val labRepository: LabRepository,
    private val conferenceRepository: ConferenceRepository,
    private val mainImageService: MainImageService
) : SearchDocumentProvider {
    override fun collectAll(): List<SearchDocument> {
        val translations = researchRepository.findAll().flatMap { it.translations }
        val (centers, groups) = translations.partition {
            it.research.postType == ResearchType.CENTERS
        }
        return research(groups, SearchType.RESEARCH_GROUP, "groups") +
            research(centers, SearchType.RESEARCH_CENTER, "centers") +
            SearchDocument.bilingual(
                rows = labRepository.findAll().flatMap { it.translations },
                type = SearchType.LAB,
                groupBy = { it.lab.id },
                sourceId = { it.lab.id },
                language = { it.language },
                title = { it.name },
                body = { searchTextOf(it) },
                url = { "/research/labs/${it.lab.id}" }
            ) + SearchDocument.bilingual(
            // 학회는 부모가 없다. 약칭이 한/영 공통이라 그걸로 묶는다.
            rows = conferenceRepository.findAll(),
            type = SearchType.CONFERENCE,
            groupBy = { it.abbreviation },
            sourceId = { it.id },
            language = { it.language },
            title = { it.name },
            body = { searchTextOf(it) },
            url = { "/research/top-conference-list" }
        )
    }

    private fun research(
        translations: List<ResearchTranslationEntity>,
        type: SearchType,
        pathSegment: String
    ) = SearchDocument.bilingual(
        rows = translations,
        type = type,
        groupBy = { it.research.id },
        sourceId = { it.research.id },
        language = { it.language },
        title = { it.name },
        body = { searchTextOf(it) },
        url = { "/research/$pathSegment/${it.research.id}" },
        thumbnailUrl = { mainImageService.createImageURL(it.research.mainImage) }
    )
}

/** 검색 대상 텍스트. 딸린 이름(연구실·교수)은 같은 언어판에서 가져온다. */
private fun searchTextOf(translation: ResearchTranslationEntity) = StringBuilder().apply {
    val research = translation.research
    appendLine(translation.name)
    appendLine(research.postType.krName)
    translation.description?.let { appendLine(cleanTextFromHtml(it)) }
    research.labs.forEach { lab ->
        lab.translationOf(translation.language)?.let { appendLine(it.name) }
    }
    research.websiteURL?.let { appendLine(it) }
}.toString()

private fun searchTextOf(translation: LabTranslationEntity) = StringBuilder().apply {
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
    translation.description?.let { appendLine(cleanTextFromHtml(it)) }
    lab.websiteURL?.let { appendLine(it) }
}.toString()

private fun searchTextOf(conference: ConferenceEntity) = StringBuilder().apply {
    appendLine(conference.name)
    appendLine(conference.abbreviation)
}.toString()
