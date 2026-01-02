package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import jakarta.persistence.*

@Entity(name = "scholarship_language")
class ScholarshipLanguageEntity(
    @OneToOne
    @JoinColumn(name = "korean_id")
    val koScholarship: ScholarshipEntity,

    @OneToOne
    @JoinColumn(name = "english_id")
    val enScholarship: ScholarshipEntity
) : BaseTimeEntity()
