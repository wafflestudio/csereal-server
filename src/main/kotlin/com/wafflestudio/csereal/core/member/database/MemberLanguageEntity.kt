package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.core.member.type.MemberType
import jakarta.persistence.*

@Entity(name = "member_language")
class MemberLanguageEntity(
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val type: MemberType,

    @Column(nullable = false)
    val koreanId: Long,

    @Column(nullable = false)
    val englishId: Long
) : BaseTimeEntity() {
    companion object {
        fun of(
            type: MemberType,
            koreanId: Long,
            englishId: Long
        ) = MemberLanguageEntity(type, koreanId, englishId)
    }
}
