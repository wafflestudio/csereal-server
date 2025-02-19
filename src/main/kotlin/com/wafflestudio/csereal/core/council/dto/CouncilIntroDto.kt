package com.wafflestudio.csereal.core.council.dto

import com.wafflestudio.csereal.core.council.database.CouncilEntity
import com.wafflestudio.csereal.core.council.database.CouncilType

data class CouncilIntroDto(
    val description: String,
    val sequence: Int,
    val name: String,
    val imageURL: String?
) {
    companion object {
        fun of(councilEntity: CouncilEntity, imageURL: String?): CouncilIntroDto {
            require(councilEntity.type == CouncilType.INTRO) {
                "CouncilEntity must be of type REPORT, but was ${councilEntity.type}"
            }
            return CouncilIntroDto(
                description = councilEntity.description,
                sequence = councilEntity.sequence,
                name = councilEntity.name,
                imageURL = imageURL
            )
        }
    }
}

data class CouncilIntroUpdateRequest(
    val description: String,
    val sequence: Int,
    val name: String,
    val removeImage: Boolean
)
