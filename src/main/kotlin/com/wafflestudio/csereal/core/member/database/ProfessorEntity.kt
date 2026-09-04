package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.entity.MainImageAttachable
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.research.database.LabEntity
import com.wafflestudio.csereal.core.resource.mainImage.database.MainImageEntity
import jakarta.persistence.*
import java.time.LocalDate

// 교수 자체. 사진·연락처·소속처럼 언어와 무관한 것만 든다.
// office 는 여기 없다 — 호실 표기가 언어마다 달라 번역본이 든다.
@Entity(name = "professor")
class ProfessorEntity(
    @Enumerated(EnumType.STRING)
    var status: ProfessorStatus,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id")
    var lab: LabEntity? = null,

    var startDate: LocalDate? = null,
    var endDate: LocalDate? = null,
    var phone: String? = null,
    var fax: String? = null,
    var email: String? = null,
    var website: String? = null,

    @OneToOne
    override var mainImage: MainImageEntity? = null,

    @OneToMany(mappedBy = "professor", cascade = [CascadeType.ALL], orphanRemoval = true)
    var translations: MutableList<ProfessorTranslationEntity> = mutableListOf()
) : BaseTimeEntity(), MainImageAttachable {
    fun translationOf(language: LanguageType): ProfessorTranslationEntity? =
        translations.firstOrNull { it.language == language }

    fun addLab(lab: LabEntity) {
        this.lab?.professors?.remove(this)
        this.lab = lab
        lab.professors.add(this)
    }
}

enum class ProfessorStatus(
    val krValue: String
) {
    ACTIVE("교수"),
    INACTIVE("역대 교수"),
    VISITING("객원교수");
}
