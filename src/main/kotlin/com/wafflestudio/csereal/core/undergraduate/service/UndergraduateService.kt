package com.wafflestudio.csereal.core.undergraduate.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.undergraduate.database.UndergraduateEntity
import com.wafflestudio.csereal.core.undergraduate.database.UndergraduateRepository
import com.wafflestudio.csereal.core.undergraduate.dto.UndergraduateDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface UndergraduateService {
    fun createUndergraduate(postType: String, request: UndergraduateDto): UndergraduateDto
    fun readUndergraduate(postType: String): UndergraduateDto
    /*
    fun createUnderCourseDependency(request: UndergraduateDto): UndergraduateDto
    fun readUnderCourseDependency(): UndergraduateDto
     */
}

@Service
class UndergraduateServiceImpl(
    private val undergraduateRepository: UndergraduateRepository,
) : UndergraduateService {
    @Transactional
    override fun createUndergraduate(postType: String, request: UndergraduateDto): UndergraduateDto {
        val newUndergraduate = UndergraduateEntity.of(postType, request)

        undergraduateRepository.save(newUndergraduate)

        return UndergraduateDto.of(newUndergraduate)
    }

    @Transactional(readOnly = true)
    override fun readUndergraduate(postType: String): UndergraduateDto {
        val undergraduate : UndergraduateEntity = undergraduateRepository.findByPostType(postType)
            ?: throw CserealException.Csereal400("존재하지 않는 게시글입니다.")

        return UndergraduateDto.of(undergraduate)
    }

    /*
    @Transactional
    override fun createUnderCourseDependency(request: UndergraduateDto): UndergraduateDto {
        val newUnderCourseDependency = UndergraduateEntity(
            postType = "underCourseDependency",
            title = request.title,
            description = request.description,
            isPublic = request.isPublic
        )

        undergraduateRepository.save(newUnderCourseDependency)

        return UndergraduateDto.of(newUnderCourseDependency)
    }

    @Transactional(readOnly = true)
    override fun readUnderCourseDependency(): UndergraduateDto {
        val undergraduate : UndergraduateEntity = undergraduateRepository.findByPostType("underCourseDependency")

        return UndergraduateDto.of(undergraduate)
    }

     */
}