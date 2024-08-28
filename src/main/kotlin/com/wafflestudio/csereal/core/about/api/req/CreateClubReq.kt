package com.wafflestudio.csereal.core.about.api.req

import com.wafflestudio.csereal.core.about.database.AboutEntity

data class CreateClubReq(
    val ko: ClubDto,
    val en: ClubDto
)

data class ClubDto(
    val name: String,
    val description: String
)

data class GroupedClubDto(
    val ko: ClubDtoWithId,
    val en: ClubDtoWithId
)

data class ClubDtoWithId(
    val id: Long,
    val name: String,
    val description: String,
    val imageURL: String?
) {
    companion object {
        fun of(aboutEntity: AboutEntity, imageURL: String?): ClubDtoWithId {
            return ClubDtoWithId(aboutEntity.id, aboutEntity.name!!, aboutEntity.description, imageURL)
        }
    }
}
