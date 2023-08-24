package com.wafflestudio.csereal.core.member.dto

import com.wafflestudio.csereal.core.member.database.ProfessorEntity
import com.wafflestudio.csereal.core.resource.image.database.ImageEntity

data class SimpleProfessorDto(
    val id: Long,
    val name: String,
    val academicRank: String,
    val labId: Long?,
    val labName: String?,
    val phone: String?,
    val email: String?,
    val imageURL: String?
) {
    companion object {
        fun of(professorEntity: ProfessorEntity): SimpleProfessorDto {
            return SimpleProfessorDto(
                id = professorEntity.id,
                name = professorEntity.name,
                academicRank = professorEntity.academicRank,
                labId = professorEntity.lab?.id,
                labName = professorEntity.lab?.name,
                phone = professorEntity.phone,
                email = professorEntity.email,
                imageURL = createImageURL(professorEntity.mainImage)
            )
        }

        private fun createImageURL(image: ImageEntity?) : String? {
            return if(image != null) {
                "http://cse-dev-waffle.bacchus.io/var/myapp/image/${image.filename}.${image.extension}"
            } else null
        }
    }
}
