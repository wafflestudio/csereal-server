package com.wafflestudio.csereal.core.member.dto

import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.database.StaffEntity

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
        fun of(staffEntity: StaffEntity, imageURL: String?): StaffDto {
            return StaffDto(
                id = staffEntity.id,
                language = LanguageType.makeLowercase(staffEntity.language),
                name = staffEntity.name,
                role = staffEntity.role,
                office = staffEntity.office,
                phone = staffEntity.phone,
                email = staffEntity.email,
                tasks = staffEntity.tasks.map { it.name },
                imageURL = imageURL
            )
        }
    }
}
