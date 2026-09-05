package com.wafflestudio.csereal.core.academics.service

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.academics.api.res.AcademicsSearchResBody
import com.wafflestudio.csereal.core.academics.database.*
import com.wafflestudio.csereal.core.main.event.RefreshSearchEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

interface AcademicsSearchService {
    fun searchTopAcademics(
        keyword: String,
        language: LanguageType,
        number: Int,
        amount: Int
    ): AcademicsSearchResBody

    fun syncCourseSearch(course: CourseEntity)
    fun syncScholarshipSearch(scholarship: ScholarshipTranslationEntity)
    fun syncAcademicsSearch(academics: AcademicsEntity)
}

@Service
class AcademicsSearchServiceImpl(
    private val academicsSearchRepository: AcademicsSearchRepository,
    private val academicsRepository: AcademicsRepository,
    private val courseRepository: CourseRepository,
    private val scholarshipTranslationRepository: ScholarshipTranslationRepository
) : AcademicsSearchService {
    @Transactional(readOnly = true)
    override fun searchTopAcademics(
        keyword: String,
        language: LanguageType,
        number: Int,
        amount: Int
    ) =
        academicsSearchRepository.searchAcademics(
            keyword = keyword,
            language = language,
            pageSize = number,
            pageNum = 1
        ).let { (acds, total) ->
            AcademicsSearchResBody.of(
                total = total,
                academics = acds,
                keyword = keyword,
                amount = amount
            )
        }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @EventListener
    fun refreshSearchListener(event: RefreshSearchEvent) {
        academicsRepository.findAll().forEach {
            syncAcademicsSearch(it)
        }

        courseRepository.findAll().forEach {
            syncCourseSearch(it)
        }

        scholarshipTranslationRepository.findAll().forEach {
            syncScholarshipSearch(it)
        }
    }

    @Transactional
    override fun syncAcademicsSearch(academics: AcademicsEntity) {
        academics.syncSearch()
    }

    @Transactional
    override fun syncScholarshipSearch(scholarship: ScholarshipTranslationEntity) {
        scholarship.syncSearch()
    }

    @Transactional
    override fun syncCourseSearch(course: CourseEntity) {
        course.syncSearch()
    }
}
