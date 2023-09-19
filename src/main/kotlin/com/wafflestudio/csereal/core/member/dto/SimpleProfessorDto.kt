package com.wafflestudio.csereal.core.member.dto

import com.wafflestudio.csereal.core.member.database.ProfessorEntity

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
        fun of(professorEntity: ProfessorEntity, imageURL: String?): SimpleProfessorDto {
            return SimpleProfessorDto(
                id = professorEntity.id,
                name = professorEntity.name,
                academicRank = professorEntity.academicRank,
                labId = professorEntity.lab?.id,
                labName = professorEntity.lab?.name,
                phone = professorEntity.phone,
                email = professorEntity.email,
                imageURL = imageURL
            )
        }
    }
}
