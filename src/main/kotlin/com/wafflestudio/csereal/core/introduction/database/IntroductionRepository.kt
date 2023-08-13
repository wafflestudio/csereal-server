package com.wafflestudio.csereal.core.introduction.database

import org.springframework.data.jpa.repository.JpaRepository

interface IntroductionRepository : JpaRepository<IntroductionEntity, Long> {
    fun findAllByPostTypeOrderByPostDetail(postType: String): List<IntroductionEntity>
    fun findByPostType(postType: String): IntroductionEntity
}