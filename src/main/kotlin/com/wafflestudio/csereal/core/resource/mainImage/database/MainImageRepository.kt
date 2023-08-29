package com.wafflestudio.csereal.core.resource.mainImage.database

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MainImageRepository : JpaRepository<MainImageEntity, Long> {
}