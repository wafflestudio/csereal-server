package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.core.member.type.MemberType
import org.springframework.data.jpa.repository.JpaRepository

interface MemberLanguageRepository : JpaRepository<MemberLanguageEntity, Long> {
    fun existsByKoreanIdAndEnglishIdAndType(
        koreanId: Long,
        englishId: Long,
        type: MemberType
    ): Boolean

    fun findByKoreanIdAndEnglishIdAndType(
        koreanId: Long,
        englishId: Long,
        type: MemberType
    ): MemberLanguageEntity?
}
