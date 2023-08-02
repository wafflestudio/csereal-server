package com.wafflestudio.csereal.core.news.database

import org.springframework.data.jpa.repository.JpaRepository

interface NewsRepository : JpaRepository<NewsEntity, Long> {

}