package com.wafflestudio.csereal.core.admissions.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.admissions.api.req.AdmissionReqBody
import com.wafflestudio.csereal.core.admissions.api.req.UpdateAdmissionReq
import com.wafflestudio.csereal.core.admissions.api.res.GroupedAdmission
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

    fun readGroupedAdmission(
        mainType: AdmissionsMainType,
        postType: AdmissionsPostType
    ): GroupedAdmission

    fun updateGroupedAdmission(
        mainType: AdmissionsMainType,
        postType: AdmissionsPostType,
        updateAdmissionReq: UpdateAdmissionReq
    )
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
        ?: throw CserealException(ErrorCode.ADMISSION_NOT_FOUND)

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
            ?: throw CserealException(ErrorCode.ADMISSION_NOT_FOUND)
        val enAdmission = admissionsRepository.findByMainTypeAndPostTypeAndLanguage(
            mainType,
            postType,
            LanguageType.EN
        )?.let { AdmissionsDto.of(it) }
            ?: throw CserealException(ErrorCode.ADMISSION_NOT_FOUND)
        return GroupedAdmission(koAdmission, enAdmission)
    }

    @Transactional
    override fun updateGroupedAdmission(
        mainType: AdmissionsMainType,
        postType: AdmissionsPostType,
        updateAdmissionReq: UpdateAdmissionReq
    ) {
        val koAdmission = admissionsRepository.findByMainTypeAndPostTypeAndLanguage(
            mainType,
            postType,
            LanguageType.KO
        ) ?: throw CserealException(ErrorCode.ADMISSION_NOT_FOUND)
        val enAdmission = admissionsRepository.findByMainTypeAndPostTypeAndLanguage(
            mainType,
            postType,
            LanguageType.EN
        ) ?: throw CserealException(ErrorCode.ADMISSION_NOT_FOUND)
        koAdmission.description = updateAdmissionReq.ko
        enAdmission.description = updateAdmissionReq.en
    }
}
