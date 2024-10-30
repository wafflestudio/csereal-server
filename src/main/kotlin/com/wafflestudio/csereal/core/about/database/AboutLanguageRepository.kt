package com.wafflestudio.csereal.core.about.database

import org.springframework.data.jpa.repository.JpaRepository

interface AboutLanguageRepository : JpaRepository<AboutLanguageEntity, Long> {
    fun findByKoAbout(koAboutEntity: AboutEntity): AboutLanguageEntity?
    fun findByEnAbout(enAboutEntity: AboutEntity): AboutLanguageEntity?
    fun findAllByKoAboutPostType(postType: AboutPostType): List<AboutLanguageEntity>
}
