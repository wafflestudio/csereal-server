package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.utils.StringListConverter
import jakarta.persistence.*

// 검색 색인은 언어별이라 번역본에 붙는다.
@Entity(name = "staff_translation")
class StaffTranslationEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    var staff: StaffEntity,

    @Enumerated(EnumType.STRING)
    var language: LanguageType,

    var name: String,
    var role: String,

    // 호실은 주소 표기라 번역 대상이다("301동 316호" / "301 Building, Room 316").
    var office: String,

    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter::class)
    var tasks: MutableList<String> = mutableListOf(),

    @OneToOne(mappedBy = "staff", cascade = [CascadeType.ALL], orphanRemoval = true)
    var memberSearch: MemberSearchEntity? = null
) : BaseTimeEntity()
