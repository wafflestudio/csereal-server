package com.wafflestudio.csereal.core.member.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.member.database.StaffEntity

data class StaffDto(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var id: Long? = null,
    val language: String,
    val name: String,
    val role: String,
    val office: String,
    val phone: String,
    val email: String,
    val tasks: List<String>,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val imageURL: String? = null
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
