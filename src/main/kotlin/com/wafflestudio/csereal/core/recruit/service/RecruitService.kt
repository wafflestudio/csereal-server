package com.wafflestudio.csereal.core.recruit.service

import com.wafflestudio.csereal.core.recruit.database.RecruitRepository
import com.wafflestudio.csereal.core.recruit.dto.RecruitPage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface RecruitService {
    fun getRecruitPage(): RecruitPage
}

@Service
@Transactional
class RecruitServiceImpl(
    private val recruitRepository: RecruitRepository
) : RecruitService {

    @Transactional(readOnly = true)
    override fun getRecruitPage(): RecruitPage {
        // return empty page if not exists
        return recruitRepository.findAll().firstOrNull()
            ?.let { RecruitPage.of(it, mainImageService.createImageURL(it.mainImage)) }
            ?: RecruitPage.empty()
    }
}
