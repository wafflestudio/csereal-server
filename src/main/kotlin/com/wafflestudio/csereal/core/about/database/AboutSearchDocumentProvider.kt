package com.wafflestudio.csereal.core.about.database

import com.wafflestudio.csereal.common.enums.LanguageType
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
            title = { it.name ?: singletonPageTitle(it.about.postType, it.language) },
            body = { searchTextOf(it, futureCareerNames) },
            url = { "/about/${it.about.postType.toValue()}" },
            thumbnailUrl = { mainImageService.createImageURL(it.about.mainImage) }
        )
    }
}

/**
 * 페이지 하나가 곧 콘텐츠인 about 은 DB 에 이름이 없다 — 이름 칼럼은 항목이 여럿인
 * 종류(동아리·시설·찾아오시는 길)를 위한 것이다. 그대로 두면 제목 없는 문서가 되어
 * 응답 조립(`SearchQueryService.toElement`)에서 통째로 버려진다: 학부 소개·인사말·
 * 연혁·연락처·졸업생 진로가 어떤 검색어로도 결과에 안 나왔다.
 *
 * 사이트 메뉴에 쓰는 이름을 색인 제목으로 준다. 제목은 본문보다 가중치가 높아
 * ("titleKo^3") '인사말'로 검색하면 그 페이지가 위로 온다.
 */
private val SINGLETON_PAGE_TITLES = mapOf(
    AboutPostType.OVERVIEW to ("학부 소개" to "Department Overview"),
    AboutPostType.GREETINGS to ("학부장 인사말" to "Greetings from the Head"),
    AboutPostType.HISTORY to ("연혁" to "History"),
    AboutPostType.FUTURE_CAREERS to ("졸업생 진로" to "Future Careers"),
    AboutPostType.CONTACT to ("연락처" to "Contact Us")
)

private fun singletonPageTitle(postType: AboutPostType, language: LanguageType): String? =
    SINGLETON_PAGE_TITLES[postType]?.let { (korean, english) ->
        if (language == LanguageType.KO) korean else english
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
