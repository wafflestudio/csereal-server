package com.wafflestudio.csereal.core.council.database

import org.springframework.data.jpa.repository.JpaRepository

interface CouncilRepository : JpaRepository<CouncilEntity, Long> {
    // ...
}
