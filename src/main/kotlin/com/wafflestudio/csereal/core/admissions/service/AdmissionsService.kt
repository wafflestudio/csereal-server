package com.wafflestudio.csereal.core.admissions.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.admissions.database.AdmissionPostType
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
        val stringPostTypes = listOf("early", "regular")
        val enumPostTypes = listOf(AdmissionPostType.UNDERGRADUATE_EARLY_ADMISSION, AdmissionPostType.UNDERGRADUATE_REGULAR_ADMISSION)

        if(!stringPostTypes.contains(postType)) {
            throw CserealException.Csereal404("해당하는 내용을 전송할 수 없습니다.")
        }
        val enumPostType = enumPostTypes[stringPostTypes.indexOf(postType)]

        val newAdmissions = AdmissionsEntity.of(enumPostType, request)

        admissionsRepository.save(newAdmissions)

        return AdmissionsDto.of(newAdmissions)
    }

    @Transactional
    override fun createGraduateAdmissions(request: AdmissionsDto): AdmissionsDto {
        val newAdmissions: AdmissionsEntity = AdmissionsEntity.of(AdmissionPostType.GRADUATE, request)

        admissionsRepository.save(newAdmissions)

        return AdmissionsDto.of(newAdmissions)
    }
    @Transactional(readOnly = true)
    override fun readUndergraduateAdmissions(postType: String): AdmissionsDto {
        return if (postType == "early") {
            AdmissionsDto.of(admissionsRepository.findByPostType(AdmissionPostType.UNDERGRADUATE_EARLY_ADMISSION))
        } else if (postType == "regular") {
            AdmissionsDto.of(admissionsRepository.findByPostType(AdmissionPostType.UNDERGRADUATE_REGULAR_ADMISSION))
        } else {
            throw CserealException.Csereal404("해당하는 페이지를 찾을 수 없습니다.")
        }
    }

    @Transactional(readOnly = true)
    override fun readGraduateAdmissions(): AdmissionsDto {
        return AdmissionsDto.of(admissionsRepository.findByPostType(AdmissionPostType.GRADUATE))

    }


}