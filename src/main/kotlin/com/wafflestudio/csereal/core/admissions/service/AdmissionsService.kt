package com.wafflestudio.csereal.core.admissions.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.admissions.api.req.AdmissionMigrateElem
import com.wafflestudio.csereal.core.admissions.api.req.AdmissionReqBody
import com.wafflestudio.csereal.core.admissions.api.res.AdmissionSearchResElem
import com.wafflestudio.csereal.core.admissions.database.AdmissionsEntity
import com.wafflestudio.csereal.core.admissions.database.AdmissionsRepository
import com.wafflestudio.csereal.core.admissions.dto.AdmissionsDto
import com.wafflestudio.csereal.core.admissions.type.AdmissionsMainType
import com.wafflestudio.csereal.core.admissions.type.AdmissionsPostType
import org.springframework.stereotype.Service
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

    @Transactional(readOnly = true)
    fun searchTopAdmission(keyword: String, language: LanguageType, number: Int): List<AdmissionSearchResElem>
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
    override fun searchTopAdmission(keyword: String, language: LanguageType, number: Int) =
        admissionsRepository.searchTopAdmissions(keyword, language, number).map {
            AdmissionSearchResElem.of(it)
        }

    @Transactional
    override fun migrateAdmissions(requestList: List<AdmissionMigrateElem>) = requestList.map {
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
