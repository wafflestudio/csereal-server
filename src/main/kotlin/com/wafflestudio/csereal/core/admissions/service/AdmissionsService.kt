package com.wafflestudio.csereal.core.admissions.service

import com.wafflestudio.csereal.core.admissions.database.AdmissionsEntity
import com.wafflestudio.csereal.core.admissions.database.AdmissionsRepository
import com.wafflestudio.csereal.core.admissions.dto.AdmissionsDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface AdmissionsService {
    fun createAdmissions(postType: String, admissionsType: String, request: AdmissionsDto) : AdmissionsDto
    // fun readAdmissionsUndergraduate() : List<AdmissionsDto>
    fun readAdmissions(postType: String): List<AdmissionsDto>
}

@Service
class AdmissionsServiceImpl(
    private val admissionsRepository: AdmissionsRepository
): AdmissionsService {
    @Transactional
    override fun createAdmissions(postType: String, admissionsType: String, request: AdmissionsDto) : AdmissionsDto {
        val newAdmissions : AdmissionsEntity = AdmissionsEntity.of(postType, admissionsType, request)

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
    override fun readAdmissions(postType: String): List<AdmissionsDto> {
        val list : MutableList<AdmissionsDto> = mutableListOf()
        if(postType == "undergraduate") {
            val susi = AdmissionsDto.of(admissionsRepository.findByAdmissionsType("susi"))
            val jeongsi = AdmissionsDto.of(admissionsRepository.findByAdmissionsType("jeongsi"))
            list.add(susi)
            list.add(jeongsi)
        } else {
            // 원래는 regular와 함께 labVideo도 같이 있습니다. labVideo도 한번에 읽을거면 리스트를 추가할 예정입니다.
            val regular = AdmissionsDto.of(admissionsRepository.findByAdmissionsType("regular"))
            list.add(regular)
        }

        return list
    }



}