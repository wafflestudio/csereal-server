package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.enums.LanguageType
import org.springframework.data.jpa.repository.JpaRepository

interface LabRepository : JpaRepository<LabEntity, Long> {
    fun findAllByLanguageOrderByName(languageType: LanguageType): List<LabEntity>
    fun findByIdAndLanguage(id: Long, languageType: LanguageType): LabEntity?
}
