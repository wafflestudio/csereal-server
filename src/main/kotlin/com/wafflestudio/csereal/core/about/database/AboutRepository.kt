package com.wafflestudio.csereal.core.about.database

import org.springframework.data.jpa.repository.JpaRepository

interface AboutRepository : JpaRepository<AboutEntity, Long> {
    fun findAllByPostTypeOrderByName(postType: AboutPostType): List<AboutEntity>
    fun findByPostType(postType: AboutPostType): AboutEntity
}
