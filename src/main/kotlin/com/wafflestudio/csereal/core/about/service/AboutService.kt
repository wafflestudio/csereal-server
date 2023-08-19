package com.wafflestudio.csereal.core.about.service

import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.core.about.database.AboutEntity
import com.wafflestudio.csereal.core.about.database.AboutPostType
import com.wafflestudio.csereal.core.about.database.AboutRepository
import com.wafflestudio.csereal.core.about.database.LocationEntity
import com.wafflestudio.csereal.core.about.dto.AboutDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface AboutService {
    fun createAbout(postType: String, request: AboutDto): AboutDto
    fun readAbout(postType: String): AboutDto
    fun readAllClubs() : List<AboutDto>
    fun readAllFacilities() : List<AboutDto>
    fun readAllDirections(): List<AboutDto>
}

@Service
class AboutServiceImpl(
    private val aboutRepository: AboutRepository
) : AboutService {
    val stringPostTypes = listOf("overview", "history", "future-careers", "contact", "student-clubs", "facilities", "directions")
    val enumPostTypes = listOf(
        AboutPostType.OVERVIEW,
        AboutPostType.HISTORY,
        AboutPostType.FUTURE_CAREERS,
        AboutPostType.CONTACT,
        AboutPostType.STUDENT_CLUBS,
        AboutPostType.FACILITIES,
        AboutPostType.DIRECTIONS
    )
    @Transactional
    override fun createAbout(postType: String, request: AboutDto): AboutDto {
        if(!stringPostTypes.contains(postType)) {
            throw CserealException.Csereal404("해당하는 내용을 전송할 수 없습니다.")
        }

        val enumPostType = enumPostTypes[stringPostTypes.indexOf(postType)]
        val newAbout = AboutEntity.of(enumPostType, request)

        if(request.locations != null) {
            for (location in request.locations) {
                LocationEntity.create(location, newAbout)
            }
        }

        aboutRepository.save(newAbout)

        return AboutDto.of(newAbout)
    }

    @Transactional(readOnly = true)
    override fun readAbout(postType: String): AboutDto {
        if(!stringPostTypes.contains(postType)) {
            throw CserealException.Csereal404("해당하는 페이지를 찾을 수 없습니다.")
        }
        val about = aboutRepository.findByPostType(enumPostTypes[stringPostTypes.indexOf(postType)])

        return AboutDto.of(about)
    }

    @Transactional(readOnly = true)
    override fun readAllClubs(): List<AboutDto> {
        val clubs = aboutRepository.findAllByPostTypeOrderByName(AboutPostType.STUDENT_CLUBS).map {
            AboutDto.of(it)
        }

        return clubs
    }

    @Transactional(readOnly = true)
    override fun readAllFacilities(): List<AboutDto> {
        val facilities = aboutRepository.findAllByPostTypeOrderByName(AboutPostType.FACILITIES).map {
            AboutDto.of(it)
        }

        return facilities
    }

    @Transactional(readOnly = true)
    override fun readAllDirections(): List<AboutDto> {
        val directions = aboutRepository.findAllByPostTypeOrderByName(AboutPostType.DIRECTIONS).map {
            AboutDto.of(it)
        }

        return directions
    }
}