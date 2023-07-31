package com.wafflestudio.csereal.core.member.dto

import com.wafflestudio.csereal.core.member.database.CareerEntity

data class CareerDto(
    val duration: String,
    val name: String,
    val workplace: String
) {
    companion object {
        fun of(careerEntity: CareerEntity): CareerDto {
            return CareerDto(
                duration = careerEntity.duration,
                name = careerEntity.name,
                workplace = careerEntity.workplace
            )
        }
    }
}
