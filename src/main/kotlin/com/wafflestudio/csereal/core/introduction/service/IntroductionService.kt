package com.wafflestudio.csereal.core.introduction.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.introduction.database.IntroductionEntity
import com.wafflestudio.csereal.core.introduction.database.IntroductionRepository
import com.wafflestudio.csereal.core.introduction.database.LocationEntity
import com.wafflestudio.csereal.core.introduction.dto.IntroductionDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface IntroductionService {
    fun createIntroduction(postType: String, postDetail: String?, request: IntroductionDto): IntroductionDto
    fun readIntroduction(postType: String): IntroductionDto
    fun readAllClubs() : List<IntroductionDto>
    fun readAllFacilities() : List<IntroductionDto>
    fun readAllDirections(): List<IntroductionDto>
}

@Service
class IntroductionServiceImpl(
    private val introductionRepository: IntroductionRepository
) : IntroductionService {
    @Transactional
    override fun createIntroduction(postType: String, postDetail: String?, request: IntroductionDto): IntroductionDto {
        val newIntroduction = IntroductionEntity.of(postType, postDetail, request)

        if(request.locations != null) {
            for (location in request.locations) {
                LocationEntity.create(location, newIntroduction)
            }
        }

        introductionRepository.save(newIntroduction)

        return IntroductionDto.of(newIntroduction)
    }

    @Transactional(readOnly = true)
    override fun readIntroduction(postType: String): IntroductionDto {
        val introduction = introductionRepository.findByPostType(postType)

        return IntroductionDto.of(introduction)
    }

    @Transactional(readOnly = true)
    override fun readAllClubs(): List<IntroductionDto> {
        val clubs = introductionRepository.findAllByPostTypeOrderByPostDetail("students-clubs").map {
            IntroductionDto.of(it)
        }

        return clubs
    }

    @Transactional(readOnly = true)
    override fun readAllFacilities(): List<IntroductionDto> {
        val facilities = introductionRepository.findAllByPostTypeOrderByPostDetail("facilities").map {
            IntroductionDto.of(it)
        }

        return facilities
    }

    @Transactional(readOnly = true)
    override fun readAllDirections(): List<IntroductionDto> {
        val directions = introductionRepository.findAllByPostTypeOrderByPostDetail("directions").map {
            IntroductionDto.of(it)
        }

        return directions
    }
}