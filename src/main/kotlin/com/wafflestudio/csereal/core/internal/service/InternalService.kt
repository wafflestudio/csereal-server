package com.wafflestudio.csereal.core.internal.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.core.internal.database.InternalEntity
import com.wafflestudio.csereal.core.internal.database.InternalRepository
import com.wafflestudio.csereal.core.internal.dto.InternalDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface InternalService {
    fun getInternal(): InternalDto
    fun modifyInternal(updateDto: InternalDto): InternalDto
}

@Service
class InternalServiceImpl(
    private val internalRepository: InternalRepository
) : InternalService {
    @Transactional
    override fun getInternal(): InternalDto =
        if (internalRepository.count() == 0L) {
            throw CserealException(ErrorCode.INTERNAL_NOT_FOUND)
        } else {
            internalRepository.findFirstByOrderByModifiedAtDesc().let {
                InternalDto.from(it)
            }
        }

    @Transactional
    override fun modifyInternal(updateDto: InternalDto): InternalDto =
        when (internalRepository.count()) {
            0L -> internalRepository.save(InternalEntity.of(updateDto))

            1L -> internalRepository.findFirstByOrderByModifiedAtDesc()
                .apply { update(updateDto) }

            else -> {
                internalRepository.deleteAll()
                internalRepository.save(InternalEntity.of(updateDto))
            }
        }.let {
            InternalDto.from(it)
        }
}
