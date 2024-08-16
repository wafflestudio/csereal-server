package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import jakarta.persistence.*

@Entity(name = "scholarship_language")
class ScholarshipLanguageEntity(
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "korean_id")
    val koScholarship: ScholarshipEntity,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "english_id")
    val enScholarship: ScholarshipEntity
) : BaseTimeEntity()
