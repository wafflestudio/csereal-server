package com.wafflestudio.csereal.core.about.database

import com.wafflestudio.csereal.common.entity.AttachmentAttachable
import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.entity.MainImageAttachable
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import jakarta.persistence.*

// 학부 소개 콘텐츠 자체. 사진·첨부처럼 언어와 무관한 것만 든다.
// 종류(post_type)도 언어판마다 다를 수 없으므로 여기 있다.
@Entity(name = "about")
class AboutEntity(
    @Enumerated(EnumType.STRING)
    var postType: AboutPostType,

    @OneToMany(mappedBy = "about", cascade = [CascadeType.ALL], orphanRemoval = true)
    override var attachments: MutableList<AttachmentEntity> = mutableListOf(),

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    override var mainImage: MainImageEntity? = null,

    @OneToMany(mappedBy = "about", cascade = [CascadeType.ALL], orphanRemoval = true)
    var translations: MutableList<AboutTranslationEntity> = mutableListOf()
) : BaseTimeEntity(), MainImageAttachable, AttachmentAttachable {
    fun translationOf(language: LanguageType): AboutTranslationEntity? =
        translations.firstOrNull { it.language == language }

    override fun attach(attachment: AttachmentEntity) {
        attachments.add(attachment)
        attachment.about = this
    }
}
