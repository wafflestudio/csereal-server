package com.wafflestudio.csereal.core.academics.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.enums.LanguageType
import jakarta.persistence.*

// name 은 실측상 12쌍이 전부 같지만(아직 영문화가 안 됐다) 번역 대상 콘텐츠라 여기에 둔다.
// 부모로 올리면 앞으로도 번역할 수 없게 된다.
@Entity(name = "scholarship_translation")
class ScholarshipTranslationEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scholarship_id")
    var scholarship: ScholarshipEntity,

    @Enumerated(EnumType.STRING)
    var language: LanguageType,

    var name: String,

    @Column(columnDefinition = "text")
    var description: String,

    @OneToOne(mappedBy = "scholarship", cascade = [CascadeType.ALL], orphanRemoval = true)
    var academicsSearch: AcademicsSearchEntity? = null
) : BaseTimeEntity()
