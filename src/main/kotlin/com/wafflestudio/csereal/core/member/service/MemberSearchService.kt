package com.wafflestudio.csereal.core.member.service

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.main.event.RefreshSearchEvent
import com.wafflestudio.csereal.core.member.api.res.MemberSearchResBody
import com.wafflestudio.csereal.core.member.database.MemberSearchEntity
import com.wafflestudio.csereal.core.member.database.MemberSearchRepository
import com.wafflestudio.csereal.core.member.database.ProfessorRepository
import com.wafflestudio.csereal.core.member.database.StaffRepository
import com.wafflestudio.csereal.core.resource.mainImage.service.MainImageService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

interface MemberSearchService {
    fun searchTopMember(keyword: String, language: LanguageType, number: Int): MemberSearchResBody

    fun searchMember(keyword: String, language: LanguageType, pageSize: Int, pageNum: Int): MemberSearchResBody
}

@Service
class MemberSearchServiceImpl(
    private val memberSearchRepository: MemberSearchRepository,
    private val mainImageService: MainImageService,
    private val professorRepository: ProfessorRepository,
    private val staffRepository: StaffRepository
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @EventListener
    fun refreshSearchListener(event: RefreshSearchEvent) {
        professorRepository.findAll().forEach { pf ->
            pf.memberSearch?.update(pf) ?: let {
                pf.memberSearch = MemberSearchEntity.create(pf)
            }
        }

        staffRepository.findAll().forEach { st ->
            st.memberSearch?.update(st) ?: let {
                st.memberSearch = MemberSearchEntity.create(st)
            }
        }
    }
}
