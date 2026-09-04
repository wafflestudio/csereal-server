package com.wafflestudio.csereal.core.member.dto

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.database.StaffTranslationEntity

data class StaffDto(
    var id: Long,
    val language: String,
    val name: String,
    val role: String,
    val office: String,
    val phone: String,
    val email: String,
    val tasks: List<String>,
    val imageURL: String?
) {
    companion object {
        fun of(translation: StaffTranslationEntity, imageURL: String?): StaffDto = StaffDto(
            id = translation.staff.id,
            language = LanguageType.makeLowercase(translation.language),
            name = translation.name,
            role = translation.role,
            office = translation.office,
            phone = translation.staff.phone,
            email = translation.staff.email,
            tasks = translation.tasks,
            imageURL = imageURL
        )
    }
}
