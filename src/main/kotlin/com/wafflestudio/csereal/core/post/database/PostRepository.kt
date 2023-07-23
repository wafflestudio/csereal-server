package com.wafflestudio.csereal.core.post.database

import org.springframework.data.jpa.repository.JpaRepository

interface PostRepository : JpaRepository<PostEntity, Long> {
}