package com.wafflestudio.csereal.core.internal.database

import org.springframework.data.jpa.repository.JpaRepository

interface InternalRepository : JpaRepository<InternalEntity, Long> {
    fun findFirstByOrderByModifiedAtDesc(): InternalEntity
}
