package com.wafflestudio.csereal.core.recruit.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.entity.MainImageAttachable
import com.wafflestudio.csereal.common.sanitize.HtmlContentHolder
import com.wafflestudio.csereal.common.sanitize.HtmlField
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToOne

@Entity(name = "recruit")
class RecruitEntity(
    var title: String,

    @Column(columnDefinition = "text")
    var description: String,

    @OneToOne
    override var mainImage: MainImageEntity? = null
) : BaseTimeEntity(), MainImageAttachable, HtmlContentHolder {

    override fun htmlFields() = listOf(HtmlField({ description }, { description = it }))
}
