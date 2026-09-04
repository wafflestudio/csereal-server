package com.wafflestudio.csereal.core.member.dto

import com.wafflestudio.csereal.core.member.database.ProfessorTranslationEntity

data class SimpleProfessorDto(
    val id: Long,
    val name: String,
    val academicRank: String,
    val department: String,
    val status: String,
    val labId: Long?,
    val labName: String?,
    val phone: String?,
    val email: String?,
    val imageURL: String?
) {
    companion object {
        fun of(translation: ProfessorTranslationEntity, imageURL: String?): SimpleProfessorDto {
            val professor = translation.professor
            return SimpleProfessorDto(
                id = professor.id,
                name = translation.name,
                academicRank = translation.academicRank,
                department = translation.department,
                status = professor.status.toString(),
                labId = professor.lab?.id,
                labName = professor.lab?.translationOf(translation.language)?.name,
                phone = professor.phone,
                email = professor.email,
                imageURL = imageURL
            )
        }
    }
}
