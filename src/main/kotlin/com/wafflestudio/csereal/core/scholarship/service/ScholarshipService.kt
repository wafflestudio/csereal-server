package com.wafflestudio.csereal.core.scholarship.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.scholarship.database.ScholarshipRepository
import com.wafflestudio.csereal.core.scholarship.dto.ScholarshipDto
import com.wafflestudio.csereal.core.scholarship.dto.SimpleScholarshipDto
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface ScholarshipService {
    fun getScholarship(scholarshipId: Long): ScholarshipDto
}

@Service
@Transactional
class ScholarshipServiceImpl(
    private val scholarshipRepository: ScholarshipRepository
) : ScholarshipService {

    @Transactional(readOnly = true)
    override fun getScholarship(scholarshipId: Long): ScholarshipDto {
        val scholarship = scholarshipRepository.findByIdOrNull(scholarshipId)
            ?: throw CserealException.Csereal404("id: $scholarshipId 에 해당하는 장학제도를 찾을 수 없습니다")
        return ScholarshipDto.of(scholarship)
    }

}
