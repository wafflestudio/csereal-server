package com.wafflestudio.csereal.core.research.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.database.ProfessorEntity
import com.wafflestudio.csereal.core.resource.attachment.database.AttachmentEntity
import jakarta.persistence.*

// 연구실 자체. PDF·소속 그룹·소속 교수·연락처는 언어와 무관하다.
// (예전엔 PDF 가 언어별 행마다 따로 저장돼 같은 파일이 두 벌 남았다.)
@Entity(name = "lab")
class LabEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "research_id")
    var research: ResearchEntity? = null,

    @OneToOne
    var pdf: AttachmentEntity? = null,

    var acronym: String? = null,
    var tel: String? = null,
    var websiteURL: String? = null,
    var youtube: String? = null,

    @OneToMany(mappedBy = "lab")
    var professors: MutableSet<ProfessorEntity> = mutableSetOf(),

    @OneToMany(mappedBy = "lab", cascade = [CascadeType.ALL], orphanRemoval = true)
    var translations: MutableList<LabTranslationEntity> = mutableListOf()
) : BaseTimeEntity() {
    fun translationOf(language: LanguageType): LabTranslationEntity? =
        translations.firstOrNull { it.language == language }
}
