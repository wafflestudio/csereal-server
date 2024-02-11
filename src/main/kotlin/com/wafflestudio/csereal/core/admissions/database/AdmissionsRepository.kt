package com.wafflestudio.csereal.core.admissions.database

import com.wafflestudio.csereal.common.properties.LanguageType
import com.wafflestudio.csereal.core.admissions.type.AdmissionsMainType
import com.wafflestudio.csereal.core.admissions.type.AdmissionsPostType
import org.springframework.data.jpa.repository.JpaRepository

interface AdmissionsRepository : JpaRepository<AdmissionsEntity, Long> {
    fun findByMainTypeAndPostTypeAndLanguage(
        mainType: AdmissionsMainType,
        postType: AdmissionsPostType,
        language: LanguageType
    ): AdmissionsEntity?
}
