package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.search.SearchDocument
import com.wafflestudio.csereal.common.search.SearchDocumentProvider
import com.wafflestudio.csereal.common.search.SearchType
import org.springframework.stereotype.Component

@Component
class AcademicsSearchDocumentProvider(
    private val academicsRepository: AcademicsRepository,
    private val courseRepository: CourseRepository,
    private val scholarshipTranslationRepository: ScholarshipTranslationRepository
) : SearchDocumentProvider {
    override fun collectAll(): List<SearchDocument> = SearchDocument.bilingual(
        // 학사 안내와 교과목은 부모가 없다. 화면을 정하는 값들이 사실상 자연키다.
        rows = academicsRepository.findAll(),
        type = SearchType.ACADEMICS,
        groupBy = { Triple(it.studentType, it.postType, it.year) },
        sourceId = { it.id },
        language = { it.language },
        title = { it.name },
        body = { AcademicsSearchEntity.createContent(it) }
    ) + SearchDocument.bilingual(
        rows = courseRepository.findAll(),
        type = SearchType.COURSE,
        groupBy = { it.studentType to it.code },
        sourceId = { it.id },
        language = { it.language },
        title = { it.name },
        body = { AcademicsSearchEntity.createContent(it) }
    ) + SearchDocument.bilingual(
        rows = scholarshipTranslationRepository.findAll(),
        type = SearchType.SCHOLARSHIP,
        groupBy = { it.scholarship.id },
        sourceId = { it.scholarship.id },
        language = { it.language },
        title = { it.name },
        body = { AcademicsSearchEntity.createContent(it) }
    )
}
