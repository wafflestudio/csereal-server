package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.common.utils.StringListConverter
import jakarta.persistence.*

// 교수의 한 언어판. 직급·학과 표기·학력·연구분야·경력은 언어마다 다르다.
@Entity(name = "professor_translation")
class ProfessorTranslationEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id")
    var professor: ProfessorEntity,

    @Enumerated(EnumType.STRING)
    var language: LanguageType,

    var name: String,
    var academicRank: String,
    var department: String,

    // 호실은 주소 표기라 번역 대상이다("301동 502호" / "301 Building, Room 502").
    // 전화·팩스·이메일·홈페이지는 언어와 무관해 부모가 든다.
    var office: String? = null,

    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter::class)
    var educations: MutableList<String> = mutableListOf(),

    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter::class)
    var researchAreas: MutableList<String> = mutableListOf(),

    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter::class)
    var careers: MutableList<String> = mutableListOf(),

    @OneToOne(mappedBy = "professor", cascade = [CascadeType.ALL], orphanRemoval = true)
    var memberSearch: MemberSearchEntity? = null
) : BaseTimeEntity()
