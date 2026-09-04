package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.enums.LanguageType
import jakarta.persistence.*

// 장학금 자체. 학부/대학원 구분만 언어와 무관하다.
@Entity(name = "scholarship")
class ScholarshipEntity(
    @Enumerated(EnumType.STRING)
    var studentType: AcademicsStudentType,

    @OneToMany(mappedBy = "scholarship", cascade = [CascadeType.ALL], orphanRemoval = true)
    var translations: MutableList<ScholarshipTranslationEntity> = mutableListOf()
) : BaseTimeEntity() {
    fun translationOf(language: LanguageType): ScholarshipTranslationEntity? =
        translations.firstOrNull { it.language == language }
}
