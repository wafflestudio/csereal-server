package com.wafflestudio.csereal.core.admissions.service

import com.wafflestudio.csereal.core.admissions.database.AdmissionPostType
import com.wafflestudio.csereal.core.admissions.database.AdmissionsEntity
import com.wafflestudio.csereal.core.admissions.database.AdmissionsRepository
import com.wafflestudio.csereal.core.admissions.database.StudentType
import com.wafflestudio.csereal.core.admissions.dto.AdmissionsDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface AdmissionsService {
    fun createAdmissions(studentType: StudentType, request: AdmissionsDto): AdmissionsDto
    fun readAdmissionsMain(studentType: StudentType): AdmissionsDto
    fun readUndergraduateAdmissions(postType: AdmissionPostType): AdmissionsDto

}

@Service
class AdmissionsServiceImpl(
    private val admissionsRepository: AdmissionsRepository
) : AdmissionsService {
    @Transactional
    override fun createAdmissions(studentType: StudentType, request: AdmissionsDto): AdmissionsDto {
        val newAdmissions: AdmissionsEntity = AdmissionsEntity.of(studentType, request)

        admissionsRepository.save(newAdmissions)

        return AdmissionsDto.of(newAdmissions)
    }

    @Transactional(readOnly = true)
    override fun readAdmissionsMain(studentType: StudentType): AdmissionsDto {
        return if (studentType == StudentType.UNDERGRADUATE) {
            AdmissionsDto.of(admissionsRepository.findByStudentTypeAndPostType(StudentType.UNDERGRADUATE, AdmissionPostType.MAIN))
        } else {
            AdmissionsDto.of(admissionsRepository.findByStudentTypeAndPostType(StudentType.GRADUATE, AdmissionPostType.MAIN))
        }
    }

    @Transactional(readOnly = true)
    override fun readUndergraduateAdmissions(postType: AdmissionPostType): AdmissionsDto {
        return if (postType == AdmissionPostType.EARLY_ADMISSION) {
            AdmissionsDto.of(admissionsRepository.findByPostType(AdmissionPostType.EARLY_ADMISSION))
        } else {
            AdmissionsDto.of(admissionsRepository.findByPostType(AdmissionPostType.REGULAR_ADMISSION))
        }
    }


}