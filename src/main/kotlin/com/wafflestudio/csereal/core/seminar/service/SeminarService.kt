package com.wafflestudio.csereal.core.seminar.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.seminar.database.SeminarEntity
import com.wafflestudio.csereal.core.seminar.database.SeminarRepository
import com.wafflestudio.csereal.core.seminar.dto.SeminarDto
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface SeminarService {
    fun createSeminar(request: SeminarDto): SeminarDto
    fun readSeminar(seminarId: Long, keyword: String?): SeminarDto
    fun updateSeminar(seminarId: Long, request: SeminarDto): SeminarDto
}

@Service
class SeminarServiceImpl(
    private val seminarRepository: SeminarRepository
) : SeminarService {

    @Transactional
    override fun createSeminar(request: SeminarDto): SeminarDto {
        val newSeminar = SeminarEntity.of(request)

        seminarRepository.save(newSeminar)

        return SeminarDto.of(newSeminar, null)
    }

    @Transactional(readOnly = true)
    override fun readSeminar(seminarId: Long, keyword: String?): SeminarDto {
        val seminar: SeminarEntity = seminarRepository.findByIdOrNull(seminarId)
            ?: throw CserealException.Csereal404("존재하지 않는 세미나입니다.(seminarId: $seminarId)")

        if (seminar.isDeleted) throw CserealException.Csereal400("삭제된 세미나입니다. (seminarId: $seminarId)")

        val prevNext = seminarRepository.findPrevNextId(seminarId, keyword)

        return SeminarDto.of(seminar, prevNext)
    }

    @Transactional
    override fun updateSeminar(seminarId: Long, request: SeminarDto): SeminarDto {
        val seminar: SeminarEntity = seminarRepository.findByIdOrNull(seminarId)
            ?: throw CserealException.Csereal404("존재하지 않는 세미나입니다")
        if(seminar.isDeleted) throw CserealException.Csereal404("삭제된 세미나입니다. (seminarId: $seminarId)")

        seminar.update(request)

        return SeminarDto.of(seminar, null)
    }
}