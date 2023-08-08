package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.member.dto.StaffDto
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany

@Entity(name = "staff")
class StaffEntity(
    var name: String,
    var role: String,

    // profileImage

    var office: String,
    var phone: String,
    var email: String,

    @OneToMany(mappedBy = "staff", cascade = [CascadeType.ALL], orphanRemoval = true)
    val tasks: MutableList<TaskEntity> = mutableListOf()

) : BaseTimeEntity() {

    companion object {
        fun of(staffDto: StaffDto): StaffEntity {
            return StaffEntity(
                name = staffDto.name,
                role = staffDto.role,
                office = staffDto.office,
                phone = staffDto.phone,
                email = staffDto.email
            )
        }
    }

    fun update(staffDto: StaffDto) {
        this.name = staffDto.name
        this.role = staffDto.role
        this.office = staffDto.office
        this.phone = staffDto.phone
        this.email = staffDto.email
    }
}
