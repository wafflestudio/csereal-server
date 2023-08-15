package com.wafflestudio.csereal.core.admissions.service

import com.wafflestudio.csereal.core.admissions.database.AdmissionsEntity
import com.wafflestudio.csereal.core.admissions.database.AdmissionsRepository
import com.wafflestudio.csereal.core.admissions.dto.AdmissionsDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface AdmissionsService {
    fun createAdmissionsUndergraduate(admissionsType: String, request: AdmissionsDto) : AdmissionsDto
    fun readAdmissionsUndergraduate() : List<AdmissionsDto>
}

@Service
class AdmissionsServiceImpl(
    private val admissionsRepository: AdmissionsRepository
): AdmissionsService {
    @Transactional
    override fun createAdmissionsUndergraduate(admissionsType: String, request: AdmissionsDto) : AdmissionsDto {
        val newAdmissions : AdmissionsEntity = AdmissionsEntity.of(admissionsType, request)

        admissionsRepository.save(newAdmissions)

        return AdmissionsDto.of(newAdmissions)
    }

    @Transactional(readOnly = true)
    override fun readAdmissionsUndergraduate() : List<AdmissionsDto> {
        val list : MutableList<AdmissionsDto> = mutableListOf()
        val susi = AdmissionsDto.of(admissionsRepository.findByAdmissionsType("susi"))
        val jeongsi = AdmissionsDto.of(admissionsRepository.findByAdmissionsType("jeongsi"))
        list.add(susi)
        list.add(jeongsi)
        return list
    }

}