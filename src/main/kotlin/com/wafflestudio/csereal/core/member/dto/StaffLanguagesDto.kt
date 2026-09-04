package com.wafflestudio.csereal.core.member.dto

import com.wafflestudio.csereal.core.member.database.StaffEntity
import com.wafflestudio.csereal.core.member.database.StaffTranslationEntity
import com.wafflestudio.csereal.common.enums.LanguageType

// 요청 본문과 같은 모양이다 — 언어와 무관한 값은 최상위, 언어별 값만 ko/en 안에.
// 예전엔 공유값이 ko·en 양쪽에 복제돼 있어서 화면이 "아무 쪽에서나 꺼내" 쓰고 있었다.
data class StaffLanguagesDto(
    val id: Long,
    val phone: String,
    val email: String,
    val imageURL: String?,
    val ko: StaffTranslationDto?,
    val en: StaffTranslationDto?
) {
    companion object {
        fun of(staff: StaffEntity, imageURL: String?) = StaffLanguagesDto(
            id = staff.id,
            phone = staff.phone,
            email = staff.email,
            imageURL = imageURL,
            ko = staff.translationOf(LanguageType.KO)?.let { StaffTranslationDto.of(it) },
            en = staff.translationOf(LanguageType.EN)?.let { StaffTranslationDto.of(it) }
        )
    }
}

data class StaffTranslationDto(
    val name: String,
    val role: String,
    // 호실 표기는 언어판마다 다르다.
    val office: String,
    val tasks: List<String>
) {
    companion object {
        fun of(translation: StaffTranslationEntity) = StaffTranslationDto(
            name = translation.name,
            role = translation.role,
            office = translation.office,
            tasks = translation.tasks
        )
    }
}
