package com.wafflestudio.csereal.core.academics.service

import com.wafflestudio.csereal.core.academics.database.AcademicsSearchRepository
import com.wafflestudio.csereal.core.academics.dto.AcademicsSearchPageResponse
import com.wafflestudio.csereal.core.academics.dto.AcademicsSearchTopResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface AcademicsSearchService {
    fun searchAcademics(keyword: String, pageSize: Int, pageNum: Int): AcademicsSearchPageResponse
    fun searchTopAcademics(keyword: String, number: Int): AcademicsSearchTopResponse
}

@Service
class AcademicsSearchServiceImpl(
    private val academicsSearchRepository: AcademicsSearchRepository
) : AcademicsSearchService {
    @Transactional(readOnly = true)
    override fun searchTopAcademics(keyword: String, number: Int) =
        AcademicsSearchTopResponse.of(
            academicsSearchRepository.searchTopAcademics(
                keyword = keyword,
                number = number
            )
        )

    @Transactional(readOnly = true)
    override fun searchAcademics(keyword: String, pageSize: Int, pageNum: Int) =
        academicsSearchRepository.searchAcademics(
            keyword = keyword,
            pageSize = pageSize,
            pageNum = pageNum
        ).let {
            AcademicsSearchPageResponse.of(
                academics = it.first,
                total = it.second
            )
        }
}
