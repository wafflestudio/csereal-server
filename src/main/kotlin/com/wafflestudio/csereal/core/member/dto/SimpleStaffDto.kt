package com.wafflestudio.csereal.core.member.dto

import com.wafflestudio.csereal.core.member.database.StaffTranslationEntity

data class SimpleStaffDto(
    val id: Long,
    val name: String,
    val role: String,
    val office: String,
    val phone: String,
    val email: String,
    val imageURL: String?
) {
    companion object {
        fun of(translation: StaffTranslationEntity, imageURL: String?): SimpleStaffDto = SimpleStaffDto(
            id = translation.staff.id,
            name = translation.name,
            role = translation.role,
            office = translation.office,
            phone = translation.staff.phone,
            email = translation.staff.email,
            imageURL = imageURL
        )
    }
}
