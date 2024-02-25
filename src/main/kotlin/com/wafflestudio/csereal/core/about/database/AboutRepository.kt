package com.wafflestudio.csereal.core.about.database

import com.wafflestudio.csereal.common.properties.LanguageType
import org.springframework.data.jpa.repository.JpaRepository

interface AboutRepository : JpaRepository<AboutEntity, Long> {
    fun findAllByLanguageAndPostTypeOrderByName(
        languageType: LanguageType,
        postType: AboutPostType
    ): List<AboutEntity>
    fun findByLanguageAndPostType(
        languageType: LanguageType,
        postType: AboutPostType
    ): AboutEntity
}
