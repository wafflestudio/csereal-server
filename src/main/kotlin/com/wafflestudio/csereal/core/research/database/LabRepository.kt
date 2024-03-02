package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.properties.LanguageType
import org.springframework.data.jpa.repository.JpaRepository

interface LabRepository : JpaRepository<LabEntity, Long> {
    fun findAllByLanguageOrderByName(languageType: LanguageType): List<LabEntity>
    fun findByName(name: String): LabEntity?
}
