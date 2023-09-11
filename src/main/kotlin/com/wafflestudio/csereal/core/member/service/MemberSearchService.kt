package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.core.member.database.MemberSearchRepository
import com.wafflestudio.csereal.core.member.dto.MemberSearchPageResponse
import com.wafflestudio.csereal.core.member.dto.MemberSearchResponseElement
import com.wafflestudio.csereal.core.member.dto.MemberSearchTopResponse
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface MemberSearchService {
    fun searchTopMember(keyword: String, number: Int): MemberSearchTopResponse
    fun searchMember(keyword: String, pageSize: Int, pageNum: Int): MemberSearchPageResponse
}

@Service
class MemberSearchServiceImpl (
        private val memberSearchRepository: MemberSearchRepository,
        private val mainImageService: MainImageService,
): MemberSearchService {
    @Transactional(readOnly = true)
    override fun searchTopMember(keyword: String, number: Int): MemberSearchTopResponse {
        val entityResults = memberSearchRepository.searchTopMember(keyword, number)
        return MemberSearchTopResponse.of(entityResults, mainImageService::createImageURL)
    }

    @Transactional(readOnly = true)
    override fun searchMember(keyword: String, pageSize: Int, pageNum: Int): MemberSearchPageResponse {
        val (entityResults, total) = memberSearchRepository.searchMember(keyword, pageSize, pageNum)
        return MemberSearchPageResponse.of(entityResults, total, mainImageService::createImageURL)
    }
}