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
    fun syncScholarshipSearch(scholarship: ScholarshipEntity)
    fun syncAcademicsSearch(academics: AcademicsEntity)
}

@Service
class AcademicsSearchServiceImpl(
    private val academicsSearchRepository: AcademicsSearchRepository,
    private val academicsRepository: AcademicsRepository,
    private val courseRepository: CourseRepository,
    private val scholarshipRepository: ScholarshipRepository
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

        scholarshipRepository.findAll().forEach {
            syncScholarshipSearch(it)
        }
    }

    @Transactional
    override fun syncAcademicsSearch(academics: AcademicsEntity) {
        academics.academicsSearch?.update(academics)
            ?: let {
                academics.academicsSearch = AcademicsSearchEntity.create(academics)
            }
    }

    @Transactional
    override fun syncScholarshipSearch(scholarship: ScholarshipEntity) {
        scholarship.academicsSearch?.update(scholarship)
            ?: let {
                scholarship.academicsSearch = AcademicsSearchEntity.create(scholarship)
            }
    }

    @Transactional
    override fun syncCourseSearch(course: CourseEntity) {
        course.academicsSearch?.update(course)
            ?: let {
                course.academicsSearch = AcademicsSearchEntity.create(course)
            }
    }
}
