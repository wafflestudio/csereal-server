package com.wafflestudio.csereal.core.admissions.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.admissions.api.req.AdmissionMigrateElem
import com.wafflestudio.csereal.core.admissions.api.req.AdmissionReqBody
import com.wafflestudio.csereal.core.admissions.api.res.AdmissionSearchResBody
import com.wafflestudio.csereal.core.admissions.api.res.AdmissionSearchResElem
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

    fun migrateAdmissions(requestList: List<AdmissionMigrateElem>): List<AdmissionsDto>

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

    @Transactional
    override fun migrateAdmissions(requestList: List<AdmissionMigrateElem>) = requestList.map {
        // Todo: add admission migrate search
        val mainType = AdmissionsMainType.fromJsonValue(it.mainType)
        val postType = AdmissionsPostType.fromJsonValue(it.postType)
        val language = LanguageType.makeStringToLanguageType(it.language)
        AdmissionsEntity(
            name = it.name!!,
            mainType = mainType,
            postType = postType,
            language = language,
            description = it.description!!,
            searchContent = AdmissionsEntity.createSearchContent(
                name = it.name,
                mainType = mainType,
                postType = postType,
                language = language,
                description = it.description
            )
        )
    }.let {
        admissionsRepository.saveAll(it)
    }.map {
        AdmissionsDto.of(it)
    }
}
