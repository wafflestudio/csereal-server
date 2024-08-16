package com.wafflestudio.csereal.core.academics.database

import org.springframework.data.jpa.repository.JpaRepository

interface ScholarshipLanguageRepository : JpaRepository<ScholarshipLanguageEntity, Long> {
    fun findByKoScholarship(koScholarshipEntity: ScholarshipEntity): ScholarshipLanguageEntity?
    fun findByEnScholarship(enScholarshipEntity: ScholarshipEntity): ScholarshipLanguageEntity?
}
