package com.wafflestudio.csereal.core.about.database

import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import com.wafflestudio.csereal.common.search.SearchDocument
import com.wafflestudio.csereal.common.search.SearchDocumentProvider
import com.wafflestudio.csereal.common.search.SearchType
import org.springframework.stereotype.Component
import com.wafflestudio.csereal.common.utils.cleanTextFromHtml

@Component
class AboutSearchDocumentProvider(
    private val aboutTranslationRepository: AboutTranslationRepository,
    private val statRepository: StatRepository,
    private val companyRepository: CompanyRepository,
    private val mainImageService: MainImageService
) : SearchDocumentProvider {
    override fun collectAll(): List<SearchDocument> {
        // 졸업생 진로 페이지에 실리는 통계·기업 이름. 페이지 전체에 걸린 목록이라
        // 번역본에 딸려 오지 않는다.
        val futureCareerNames =
            statRepository.findAll().map { it.name } + companyRepository.findAll().map { it.name }

        return SearchDocument.bilingual(
            rows = aboutTranslationRepository.findAll(),
            type = SearchType.ABOUT,
            groupBy = { it.about.id },
            sourceId = { it.about.id },
            language = { it.language },
            title = { it.name },
            body = { searchTextOf(it, futureCareerNames) },
            url = { "/about/${it.about.postType.toValue()}" },
            thumbnailUrl = { mainImageService.createImageURL(it.about.mainImage) }
        )
    }
}

/** 검색 대상 텍스트. 본문은 HTML 이라 태그를 걷어내고 담는다. */
private fun searchTextOf(
    translation: AboutTranslationEntity,
    futureCareerNames: List<String>
) = StringBuilder().apply {
    translation.name?.let { appendLine(it) }
    appendLine(cleanTextFromHtml(translation.description))
    if (translation.about.postType == AboutPostType.FUTURE_CAREERS) {
        futureCareerNames.forEach { appendLine(it) }
    } else {
        translation.locations.forEach { appendLine(it) }
    }
}.toString()
