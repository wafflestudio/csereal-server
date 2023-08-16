package com.wafflestudio.csereal.core.member.dto

import com.wafflestudio.csereal.core.member.database.StaffEntity

data class SimpleStaffDto(
    val id: Long,
    val name: String,
    val role: String,
    val office: String,
    val phone: String,
    val email: String,
    val imageUri: String?
) {

    companion object {
        fun of(staffEntity: StaffEntity): SimpleStaffDto {
            return SimpleStaffDto(
                id = staffEntity.id,
                name = staffEntity.name,
                role = staffEntity.role,
                office = staffEntity.office,
                phone = staffEntity.phone,
                email = staffEntity.email,
                imageUri = staffEntity.imageUri
            )
        }
    }
}
