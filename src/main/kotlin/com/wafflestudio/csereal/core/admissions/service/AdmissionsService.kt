package com.wafflestudio.csereal.core.admissions.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.admissions.database.AdmissionsPostType
import com.wafflestudio.csereal.core.admissions.database.AdmissionsEntity
import com.wafflestudio.csereal.core.admissions.database.AdmissionsRepository
import com.wafflestudio.csereal.core.admissions.dto.AdmissionsDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface AdmissionsService {
    fun createUndergraduateAdmissions(postType: String, request: AdmissionsDto): AdmissionsDto
    fun createGraduateAdmissions(request: AdmissionsDto): AdmissionsDto
    fun readUndergraduateAdmissions(postType: String): AdmissionsDto
    fun readGraduateAdmissions(): AdmissionsDto

}

@Service
class AdmissionsServiceImpl(
    private val admissionsRepository: AdmissionsRepository
) : AdmissionsService {
    @Transactional
    override fun createUndergraduateAdmissions(postType: String, request: AdmissionsDto): AdmissionsDto {
        val enumPostType = makeStringToAdmissionsPostType(postType)

        val pageName = when(enumPostType) {
            AdmissionsPostType.UNDERGRADUATE_EARLY_ADMISSION -> "수시 모집"
            AdmissionsPostType.UNDERGRADUATE_REGULAR_ADMISSION -> "정시 모집"
            else -> throw CserealException.Csereal404("해당하는 페이지를 찾을 수 없습니다.")
        }

        val newAdmissions = AdmissionsEntity.of(enumPostType, pageName, request)

        admissionsRepository.save(newAdmissions)

        return AdmissionsDto.of(newAdmissions)
    }

    @Transactional
    override fun createGraduateAdmissions(request: AdmissionsDto): AdmissionsDto {
        val newAdmissions: AdmissionsEntity = AdmissionsEntity.of(AdmissionsPostType.GRADUATE, "전기/후기 모집", request)

        admissionsRepository.save(newAdmissions)

        return AdmissionsDto.of(newAdmissions)
    }

    @Transactional(readOnly = true)
    override fun readUndergraduateAdmissions(postType: String): AdmissionsDto {
        return when (postType) {
            "early" -> AdmissionsDto.of(admissionsRepository.findByPostType(AdmissionsPostType.UNDERGRADUATE_EARLY_ADMISSION))
            "regular" -> AdmissionsDto.of(admissionsRepository.findByPostType(AdmissionsPostType.UNDERGRADUATE_REGULAR_ADMISSION))
            else -> throw CserealException.Csereal404("해당하는 페이지를 찾을 수 없습니다.")
        }
    }

    @Transactional(readOnly = true)
    override fun readGraduateAdmissions(): AdmissionsDto {
        return AdmissionsDto.of(admissionsRepository.findByPostType(AdmissionsPostType.GRADUATE))
    }

    private fun makeStringToAdmissionsPostType(postType: String) : AdmissionsPostType {
        try {
            val upperPostType = postType.replace("-","_").uppercase()
            return AdmissionsPostType.valueOf(upperPostType)

        } catch (e: IllegalArgumentException) {
            throw CserealException.Csereal400("해당하는 enum을 찾을 수 없습니다")
        }
    }
}