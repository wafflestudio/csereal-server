package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.MainImageContentEntityType
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.utils.StringListConverter
import com.wafflestudio.csereal.core.member.dto.StaffDto
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import jakarta.persistence.*

@Entity(name = "staff")
class StaffEntity(
    @Enumerated(EnumType.STRING)
    var language: LanguageType,

    var name: String,
    var role: String,

    var office: String,
    var phone: String,
    var email: String,

    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter::class)
    var tasks: MutableList<String> = mutableListOf(),

    @OneToOne
    var mainImage: MainImageEntity? = null,

    @OneToOne(mappedBy = "staff", cascade = [CascadeType.ALL], orphanRemoval = true)
    var memberSearch: MemberSearchEntity? = null
) : BaseTimeEntity(), MainImageContentEntityType {
    override fun bringMainImage(): MainImageEntity? = mainImage

    companion object {
        fun of(languageType: LanguageType, staffDto: StaffDto): StaffEntity {
            return StaffEntity(
                language = languageType,
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
