package com.wafflestudio.csereal.core.member.dto

import com.wafflestudio.csereal.core.member.database.StaffEntity
import com.wafflestudio.csereal.core.resource.image.database.ImageEntity

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
        fun of(staffEntity: StaffEntity): SimpleStaffDto {
            return SimpleStaffDto(
                id = staffEntity.id,
                name = staffEntity.name,
                role = staffEntity.role,
                office = staffEntity.office,
                phone = staffEntity.phone,
                email = staffEntity.email,
                imageURL = createImageURL(staffEntity.mainImage)
            )
        }

        private fun createImageURL(image: ImageEntity?) : String? {
            return if(image != null) {
                "http://cse-dev-waffle.bacchus.io/var/myapp/image/${image.filename}.${image.extension}"
            } else null
        }
    }
}
