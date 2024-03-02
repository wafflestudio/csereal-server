package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.member.api.res.MemberSearchResBody
import com.wafflestudio.csereal.core.member.database.MemberSearchRepository
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface MemberSearchService {
    fun searchTopMember(keyword: String, language: LanguageType, number: Int): MemberSearchResBody

    fun searchMember(keyword: String, language: LanguageType, pageSize: Int, pageNum: Int): MemberSearchResBody
}

@Service
class MemberSearchServiceImpl(
    private val memberSearchRepository: MemberSearchRepository,
    private val mainImageService: MainImageService
) : MemberSearchService {
    @Transactional(readOnly = true)
    override fun searchTopMember(keyword: String, language: LanguageType, number: Int): MemberSearchResBody {
        val (entityResults, total) = memberSearchRepository.searchMember(keyword, language, number, 1)
        return MemberSearchResBody.of(entityResults, total, mainImageService::createImageURL)
    }

    @Transactional(readOnly = true)
    override fun searchMember(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int
    ): MemberSearchResBody {
        val (entityResults, total) = memberSearchRepository.searchMember(keyword, language, pageSize, pageNum)
        return MemberSearchResBody.of(entityResults, total, mainImageService::createImageURL)
    }
}
