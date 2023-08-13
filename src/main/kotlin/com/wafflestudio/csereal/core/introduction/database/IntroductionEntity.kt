package com.wafflestudio.csereal.core.introduction.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.introduction.dto.IntroductionDto
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity(name = "introduction")
class IntroductionEntity(
    var postType: String,

    var postDetail: String?,

    var title: String,

    var description: String,

    var year: Int?,

    var isPublic: Boolean,

    @OneToMany(mappedBy = "introduction", cascade = [CascadeType.ALL], orphanRemoval = true)
    val locations: MutableList<LocationEntity> = mutableListOf()
) : BaseTimeEntity() {
    companion object {
        fun of(introductionDto: IntroductionDto): IntroductionEntity {
            return IntroductionEntity(
                postType = introductionDto.postType,
                postDetail = introductionDto.postDetail,
                title = introductionDto.title,
                description = introductionDto.description,
                year = introductionDto.year,
                isPublic = introductionDto.isPublic,
            )
        }
    }
}