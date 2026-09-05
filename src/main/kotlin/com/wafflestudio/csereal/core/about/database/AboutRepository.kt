package com.wafflestudio.csereal.core.about.database

import org.springframework.data.jpa.repository.JpaRepository

interface AboutRepository : JpaRepository<AboutEntity, Long> {
    fun findAllByPostType(postType: AboutPostType): List<AboutEntity>

    // 싱글턴(개요·인사말·연혁·졸업생 진로·연락처)만 쓴다. 여러 행인 종류에 부르면
    // 결과가 하나가 아니라 Spring Data 가 예외를 던진다.
    fun findByPostType(postType: AboutPostType): AboutEntity
}

interface AboutTranslationRepository : JpaRepository<AboutTranslationEntity, Long>
