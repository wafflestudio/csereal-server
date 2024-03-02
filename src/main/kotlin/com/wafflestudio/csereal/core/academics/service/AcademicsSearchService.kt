package com.wafflestudio.csereal.core.academics.service

import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.academics.database.AcademicsSearchRepository
import com.wafflestudio.csereal.core.academics.api.res.AcademicsSearchResBody
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface AcademicsSearchService {
    fun searchTopAcademics(
        keyword: String,
        language: LanguageType,
        number: Int,
        amount: Int
    ): AcademicsSearchResBody
    fun searchAcademics(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int,
        amount: Int
    ): AcademicsSearchResBody
}

@Service
class AcademicsSearchServiceImpl(
    private val academicsSearchRepository: AcademicsSearchRepository
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

    @Transactional(readOnly = true)
    override fun searchAcademics(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int,
        amount: Int
    ) =
        academicsSearchRepository.searchAcademics(
            keyword = keyword,
            language = language,
            pageSize = pageSize,
            pageNum = pageNum
        ).let {
            AcademicsSearchResBody.of(
                academics = it.first,
                total = it.second,
                keyword = keyword,
                amount = amount
            )
        }
}
