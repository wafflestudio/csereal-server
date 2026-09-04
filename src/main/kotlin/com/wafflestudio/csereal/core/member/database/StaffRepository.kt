package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.enums.LanguageType
import org.springframework.data.jpa.repository.JpaRepository

interface StaffRepository : JpaRepository<StaffEntity, Long>

interface StaffTranslationRepository : JpaRepository<StaffTranslationEntity, Long> {
    fun findAllByLanguage(language: LanguageType): List<StaffTranslationEntity>
}
