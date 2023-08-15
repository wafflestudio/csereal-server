package com.wafflestudio.csereal.core.admissions.service

import com.wafflestudio.csereal.core.admissions.database.AdmissionsEntity
import com.wafflestudio.csereal.core.admissions.database.AdmissionsRepository
import com.wafflestudio.csereal.core.admissions.dto.AdmissionsDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface AdmissionsService {
    fun createAdmissions(request: AdmissionsDto): AdmissionsDto

    // fun readAdmissionsUndergraduate() : List<AdmissionsDto>
    fun readAdmissionsMain(to: String): AdmissionsDto
    fun readUndergraduateAdmissions(postType: String): AdmissionsDto

}

@Service
class AdmissionsServiceImpl(
    private val admissionsRepository: AdmissionsRepository
) : AdmissionsService {
    @Transactional
    override fun createAdmissions(request: AdmissionsDto): AdmissionsDto {
        val newAdmissions: AdmissionsEntity = AdmissionsEntity.of(request)

        admissionsRepository.save(newAdmissions)

        return AdmissionsDto.of(newAdmissions)
    }

    /*
    @Transactional(readOnly = true)
    override fun readAdmissionsUndergraduate() : List<AdmissionsDto> {

        val list : MutableList<AdmissionsDto> = mutableListOf()
        val susi = AdmissionsDto.of(admissionsRepository.findByAdmissionsType("susi"))
        val jeongsi = AdmissionsDto.of(admissionsRepository.findByAdmissionsType("jeongsi"))
        list.add(susi)
        list.add(jeongsi)
        return list
    }

     */

    @Transactional(readOnly = true)
    override fun readAdmissionsMain(to: String): AdmissionsDto {
        return if (to == "undergraduate") {
            AdmissionsDto.of(admissionsRepository.findByToAndPostType("undergraduate", "main"))
        } else {
            AdmissionsDto.of(admissionsRepository.findByToAndPostType("graduate", "main"))
        }
    }

    @Transactional(readOnly = true)
    override fun readUndergraduateAdmissions(postType: String): AdmissionsDto {
        return if (postType == "early-admission") {
            AdmissionsDto.of(admissionsRepository.findByPostType("early-admission"))
        } else {
            AdmissionsDto.of(admissionsRepository.findByPostType("regular-admission"))
        }
    }


}