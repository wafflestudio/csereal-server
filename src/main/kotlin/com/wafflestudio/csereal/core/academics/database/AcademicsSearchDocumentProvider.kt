package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.search.SearchDocument
import com.wafflestudio.csereal.common.search.SearchDocumentProvider
import com.wafflestudio.csereal.common.search.SearchType
import org.springframework.stereotype.Component
import com.wafflestudio.csereal.common.utils.cleanTextFromHtml

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
        body = { searchTextOf(it) },
        url = { "/academics/${it.studentType.toValue()}/${it.postType.toValue()}" }
    ) + SearchDocument.bilingual(
        rows = courseRepository.findAll(),
        type = SearchType.COURSE,
        groupBy = { it.studentType to it.code },
        sourceId = { it.id },
        language = { it.language },
        title = { it.name },
        body = { searchTextOf(it) },
        url = { "/academics/${it.studentType.toValue()}/courses" }
    ) + SearchDocument.bilingual(
        rows = scholarshipTranslationRepository.findAll(),
        type = SearchType.SCHOLARSHIP,
        groupBy = { it.scholarship.id },
        sourceId = { it.scholarship.id },
        language = { it.language },
        title = { it.name },
        body = { searchTextOf(it) },
        url = {
            "/academics/${it.scholarship.studentType.toValue()}/scholarship/${it.scholarship.id}"
        }
    )
}

/** 검색 대상 텍스트. 본문은 HTML 이라 태그를 걷어내고 담는다. */
private fun searchTextOf(academics: AcademicsEntity) = StringBuilder().apply {
    appendLine(academics.name)
    academics.year?.let { appendLine(it) }
    appendLine(academics.studentType.value)
    appendLine(cleanTextFromHtml(academics.description))
}.toString()

private fun searchTextOf(course: CourseEntity) = StringBuilder().apply {
    appendLine(course.studentType.value)
    appendLine(course.classification)
    appendLine(course.code)
    appendLine(course.name)
    appendLine(course.credit)
    appendLine(course.grade)
    course.description?.let { appendLine(cleanTextFromHtml(it)) }
}.toString()

private fun searchTextOf(scholarship: ScholarshipTranslationEntity) = StringBuilder().apply {
    appendLine(scholarship.scholarship.studentType.value)
    appendLine(scholarship.name)
    appendLine(cleanTextFromHtml(scholarship.description))
}.toString()
