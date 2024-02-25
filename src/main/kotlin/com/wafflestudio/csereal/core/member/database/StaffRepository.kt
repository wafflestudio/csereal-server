package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.properties.LanguageType
import org.springframework.data.jpa.repository.JpaRepository

interface StaffRepository : JpaRepository<StaffEntity, Long> {
    fun findAllByLanguage(languageType: LanguageType): List<StaffEntity>
}
