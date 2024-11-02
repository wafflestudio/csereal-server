package com.wafflestudio.csereal.core.recruit.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.controller.MainImageContentEntityType
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToOne

@Entity(name = "recruit")
class RecruitEntity(
    var latestRecruitTitle: String,
    var latestRecruitUrl: String,

    @Column(columnDefinition = "text")
    var description: String,

    @OneToOne
    var mainImage: MainImageEntity? = null
) : BaseTimeEntity(), MainImageContentEntityType {
    override fun bringMainImage(): MainImageEntity? = mainImage
}
