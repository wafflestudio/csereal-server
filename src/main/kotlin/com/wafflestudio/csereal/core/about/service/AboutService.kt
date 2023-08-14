package com.wafflestudio.csereal.core.about.service

import com.wafflestudio.csereal.core.about.database.AboutEntity
import com.wafflestudio.csereal.core.about.database.AboutRepository
import com.wafflestudio.csereal.core.about.database.LocationEntity
import com.wafflestudio.csereal.core.about.dto.AboutDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface AboutService {
    fun createAbout(request: AboutDto): AboutDto
    fun readAbout(postType: String): AboutDto
    fun readAllClubs() : List<AboutDto>
    fun readAllFacilities() : List<AboutDto>
    fun readAllDirections(): List<AboutDto>
}

@Service
class AboutServiceImpl(
    private val aboutRepository: AboutRepository
) : AboutService {
    @Transactional
    override fun createAbout(request: AboutDto): AboutDto {
        val newAbout = AboutEntity.of(request)

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
        val about = aboutRepository.findByPostType(postType)

        return AboutDto.of(about)
    }

    @Transactional(readOnly = true)
    override fun readAllClubs(): List<AboutDto> {
        val clubs = aboutRepository.findAllByPostTypeOrderByPostDetail("student-clubs").map {
            AboutDto.of(it)
        }

        return clubs
    }

    @Transactional(readOnly = true)
    override fun readAllFacilities(): List<AboutDto> {
        val facilities = aboutRepository.findAllByPostTypeOrderByPostDetail("facilities").map {
            AboutDto.of(it)
        }

        return facilities
    }

    @Transactional(readOnly = true)
    override fun readAllDirections(): List<AboutDto> {
        val directions = aboutRepository.findAllByPostTypeOrderByPostDetail("directions").map {
            AboutDto.of(it)
        }

        return directions
    }
}