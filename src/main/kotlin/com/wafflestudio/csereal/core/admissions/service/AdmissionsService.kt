package com.wafflestudio.csereal.core.admissions.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.admissions.api.req.AdmissionReqBody
import com.wafflestudio.csereal.core.admissions.api.req.UpdateAdmissionReq
import com.wafflestudio.csereal.core.admissions.api.res.AdmissionSearchResBody
import com.wafflestudio.csereal.core.admissions.api.res.AdmissionSearchResElem
import com.wafflestudio.csereal.core.admissions.api.res.GroupedAdmission
import com.wafflestudio.csereal.core.admissions.database.AdmissionsEntity
import com.wafflestudio.csereal.core.admissions.database.AdmissionsRepository
import com.wafflestudio.csereal.core.admissions.dto.AdmissionsDto
import com.wafflestudio.csereal.core.admissions.type.AdmissionsMainType
import com.wafflestudio.csereal.core.admissions.type.AdmissionsPostType
import com.wafflestudio.csereal.core.main.event.RefreshSearchEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

interface AdmissionsService {
    fun createAdmission(
        req: AdmissionReqBody,
        mainType: AdmissionsMainType,
        postType: AdmissionsPostType
    ): AdmissionsDto

    fun readAdmission(
        mainType: AdmissionsMainType,
        postType: AdmissionsPostType,
        language: LanguageType
    ): AdmissionsDto

    fun readGroupedAdmission(
        mainType: AdmissionsMainType,
        postType: AdmissionsPostType
    ): GroupedAdmission

    fun updateAdmission(
        mainType: AdmissionsMainType,
        postType: AdmissionsPostType,
        updateAdmissionReq: UpdateAdmissionReq
    )

    fun searchPageAdmission(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int,
        amount: Int
    ): AdmissionSearchResBody

    fun searchTopAdmission(keyword: String, language: LanguageType, number: Int, amount: Int): AdmissionSearchResBody
}

@Service
class AdmissionsServiceImpl(
    private val admissionsRepository: AdmissionsRepository
) : AdmissionsService {
    @Transactional
    override fun createAdmission(
        req: AdmissionReqBody,
        mainType: AdmissionsMainType,
        postType: AdmissionsPostType
    ) = admissionsRepository.save(
        AdmissionsEntity.of(mainType, postType, req)
    ).let {
        AdmissionsDto.of(it)
    }

    @Transactional(readOnly = true)
    override fun readAdmission(
        mainType: AdmissionsMainType,
        postType: AdmissionsPostType,
        language: LanguageType
    ) = admissionsRepository.findByMainTypeAndPostTypeAndLanguage(
        mainType,
        postType,
        language
    )?.let { AdmissionsDto.of(it) }
        ?: throw CserealException.Csereal404("해당하는 페이지를 찾을 수 없습니다.")

    @Transactional(readOnly = true)
    override fun readGroupedAdmission(
        mainType: AdmissionsMainType,
        postType: AdmissionsPostType
    ): GroupedAdmission {
        val koAdmission = admissionsRepository.findByMainTypeAndPostTypeAndLanguage(
            mainType,
            postType,
            LanguageType.KO
        )?.let { AdmissionsDto.of(it) }
            ?: throw CserealException.Csereal404("해당하는 한글 페이지를 찾을 수 없습니다.")
        val enAdmission = admissionsRepository.findByMainTypeAndPostTypeAndLanguage(
            mainType,
            postType,
            LanguageType.EN
        )?.let { AdmissionsDto.of(it) }
            ?: throw CserealException.Csereal404("해당하는 영어 페이지를 찾을 수 없습니다.")
        return GroupedAdmission(koAdmission, enAdmission)
    }

    @Transactional
    override fun updateAdmission(
        mainType: AdmissionsMainType,
        postType: AdmissionsPostType,
        updateAdmissionReq: UpdateAdmissionReq
    ) {
        val koAdmission = admissionsRepository.findByMainTypeAndPostTypeAndLanguage(
            mainType,
            postType,
            LanguageType.KO
        ) ?: throw CserealException.Csereal404("해당하는 한글 페이지를 찾을 수 없습니다.")
        val enAdmission = admissionsRepository.findByMainTypeAndPostTypeAndLanguage(
            mainType,
            postType,
            LanguageType.EN
        ) ?: throw CserealException.Csereal404("해당하는 한글 페이지를 찾을 수 없습니다.")
        koAdmission.description = updateAdmissionReq.ko
        enAdmission.description = updateAdmissionReq.en
        syncSearchAdmission(koAdmission)
        syncSearchAdmission(enAdmission)
    }

    @Transactional(readOnly = true)
    override fun searchTopAdmission(
        keyword: String,
        language: LanguageType,
        number: Int,
        amount: Int
    ): AdmissionSearchResBody {
        val (admissions, total) = admissionsRepository.searchAdmissions(keyword, language, number, 1)
        return AdmissionSearchResBody(
            total = total,
            admissions = admissions.map {
                AdmissionSearchResElem.of(it, keyword, amount)
            }
        )
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @EventListener
    fun refreshSearch(event: RefreshSearchEvent) {
        admissionsRepository.findAll().forEach {
            syncSearchAdmission(it)
        }
    }

    @Transactional
    fun syncSearchAdmission(admissions: AdmissionsEntity) {
        admissions.apply {
            searchContent = AdmissionsEntity.createSearchContent(
                name,
                mainType,
                postType,
                language,
                description
            )
        }
    }

    @Transactional(readOnly = true)
    override fun searchPageAdmission(
        keyword: String,
        language: LanguageType,
        pageSize: Int,
        pageNum: Int,
        amount: Int
    ): AdmissionSearchResBody {
        val (admissions, total) = admissionsRepository.searchAdmissions(keyword, language, pageSize, pageNum)
        return AdmissionSearchResBody(
            total = total,
            admissions = admissions.map {
                AdmissionSearchResElem.of(it, keyword, amount)
            }
        )
    }
}
