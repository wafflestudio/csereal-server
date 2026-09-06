package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import com.wafflestudio.csereal.common.search.SearchDocument
import com.wafflestudio.csereal.common.search.SearchDocumentProvider
import com.wafflestudio.csereal.common.search.SearchType
import org.springframework.stereotype.Component

/**
 * member_search 테이블을 거치지 않고 원본에서 바로 만든다.
 * 그 테이블은 파생 데이터라 갱신이 밀리면 낡는데, ES 가 그 낡음을 물려받을 이유가 없다.
 */
@Component
class MemberSearchDocumentProvider(
    private val professorRepository: ProfessorRepository,
    private val staffRepository: StaffRepository,
    private val mainImageService: MainImageService
) : SearchDocumentProvider {
    override fun collectAll(): List<SearchDocument> {
        val translations = professorRepository.findAll().flatMap { it.translations }
        // 현직과 역대는 화면도 경로도 다르다(/people/faculty vs /people/emeritus-faculty).
        val (emeritus, active) = translations.partition {
            it.professor.status == ProfessorStatus.INACTIVE
        }
        return professors(active, SearchType.PROFESSOR, "faculty") +
            professors(emeritus, SearchType.EMERITUS_PROFESSOR, "emeritus-faculty") +
            SearchDocument.bilingual(
                rows = staffRepository.findAll().flatMap { it.translations },
                type = SearchType.STAFF,
                groupBy = { it.staff.id },
                sourceId = { it.staff.id },
                language = { it.language },
                title = { it.name },
                body = { searchTextOf(it) },
                url = { "/people/staff/${it.staff.id}" },
                thumbnailUrl = { mainImageService.createImageURL(it.staff.mainImage) }
            )
    }

    private fun professors(
        translations: List<ProfessorTranslationEntity>,
        type: SearchType,
        pathSegment: String
    ) = SearchDocument.bilingual(
        rows = translations,
        type = type,
        groupBy = { it.professor.id },
        sourceId = { it.professor.id },
        language = { it.language },
        title = { it.name },
        body = { searchTextOf(it) },
        url = { "/people/$pathSegment/${it.professor.id}" },
        thumbnailUrl = { mainImageService.createImageURL(it.professor.mainImage) }
    )
}

/**
 * 검색 대상 텍스트. 화면에 보이지 않는 값(전화·이메일·경력)까지 담아 두면
 * "그 사람 내선번호" 같은 검색어로도 찾힌다.
 */
private fun searchTextOf(translation: ProfessorTranslationEntity): String {
    val professor = translation.professor
    val stringBuilder = StringBuilder()
    stringBuilder.appendLine(translation.name)
    stringBuilder.appendLine(professor.status.krValue)
    stringBuilder.appendLine(translation.academicRank)
    stringBuilder.appendLine(translation.department)
    // 소속 연구실 이름은 같은 언어판으로.
    professor.lab?.translationOf(translation.language)?.let { stringBuilder.appendLine(it.name) }
    professor.startDate?.let { stringBuilder.appendLine(it) }
    professor.endDate?.let { stringBuilder.appendLine(it) }
    translation.office?.let { stringBuilder.appendLine(it) }
    professor.phone?.let { stringBuilder.appendLine(it) }
    professor.fax?.let { stringBuilder.appendLine(it) }
    professor.email?.let { stringBuilder.appendLine(it) }
    professor.website?.let { stringBuilder.appendLine(it) }
    translation.educations.forEach { stringBuilder.appendLine(it) }
    translation.researchAreas.forEach { stringBuilder.appendLine(it) }
    translation.careers.forEach { stringBuilder.appendLine(it) }

    return stringBuilder.toString()
}

private fun searchTextOf(staff: StaffTranslationEntity): String {
    val stringBuilder = StringBuilder()
    stringBuilder.appendLine(staff.name)
    stringBuilder.appendLine(staff.role)
    stringBuilder.appendLine(staff.office)
    stringBuilder.appendLine(staff.staff.phone)
    stringBuilder.appendLine(staff.staff.email)
    staff.tasks.forEach { stringBuilder.appendLine(it) }

    return stringBuilder.toString()
}
