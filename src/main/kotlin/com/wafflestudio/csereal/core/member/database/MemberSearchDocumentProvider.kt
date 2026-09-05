package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.search.SearchDocument
import com.wafflestudio.csereal.common.search.SearchDocumentProvider
import com.wafflestudio.csereal.common.search.SearchType
import org.springframework.stereotype.Component

/**
 * member_search 테이블을 거치지 않고 원본에서 바로 만든다.
 * 그 테이블은 파생 데이터라 갱신이 밀리면 낡는데, ES 가 그 낡음을 물려받을 이유가 없다.
 * "무엇이 검색 대상 텍스트인가"는 여전히 createContent() 한 곳에 있다.
 */
@Component
class MemberSearchDocumentProvider(
    private val professorRepository: ProfessorRepository,
    private val staffRepository: StaffRepository
) : SearchDocumentProvider {
    override fun collectAll(): List<SearchDocument> = SearchDocument.bilingual(
        rows = professorRepository.findAll().flatMap { it.translations },
        type = SearchType.PROFESSOR,
        groupBy = { it.professor.id },
        sourceId = { it.professor.id },
        language = { it.language },
        title = { it.name },
        body = { MemberSearchEntity.createContent(it) }
    ) + SearchDocument.bilingual(
        rows = staffRepository.findAll().flatMap { it.translations },
        type = SearchType.STAFF,
        groupBy = { it.staff.id },
        sourceId = { it.staff.id },
        language = { it.language },
        title = { it.name },
        body = { MemberSearchEntity.createContent(it) }
    )
}
