package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import com.wafflestudio.csereal.common.entity.MainImageAttachable
import jakarta.persistence.*

// 행정직원 자체. 사진·연락처처럼 언어와 무관한 것만 든다.
// office 는 여기 없다 — 호실 표기가 언어마다 달라 번역본이 든다.
@Entity(name = "staff")
class StaffEntity(
    var phone: String,
    var email: String,

    @OneToOne
    override var mainImage: MainImageEntity? = null,

    @OneToMany(mappedBy = "staff", cascade = [CascadeType.ALL], orphanRemoval = true)
    var translations: MutableList<StaffTranslationEntity> = mutableListOf()
) : BaseTimeEntity(), MainImageAttachable {
    fun translationOf(language: LanguageType): StaffTranslationEntity? =
        translations.firstOrNull { it.language == language }

    fun updateShared(phone: String, email: String) {
        this.phone = phone
        this.email = email
    }
}
