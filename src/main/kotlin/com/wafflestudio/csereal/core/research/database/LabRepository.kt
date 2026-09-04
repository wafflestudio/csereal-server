package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.enums.LanguageType
import org.springframework.data.jpa.repository.JpaRepository

interface LabRepository : JpaRepository<LabEntity, Long>

interface LabTranslationRepository : JpaRepository<LabTranslationEntity, Long> {
    fun findAllByLanguageOrderByName(language: LanguageType): List<LabTranslationEntity>
}
