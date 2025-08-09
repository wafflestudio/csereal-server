package com.wafflestudio.csereal.core.about.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import jakarta.persistence.*

@Entity(name = "about_language")
class AboutLanguageEntity(
    @OneToOne
    @JoinColumn(name = "korean_id")
    val koAbout: AboutEntity,

    @OneToOne
    @JoinColumn(name = "english_id")
    val enAbout: AboutEntity
) : BaseTimeEntity()
