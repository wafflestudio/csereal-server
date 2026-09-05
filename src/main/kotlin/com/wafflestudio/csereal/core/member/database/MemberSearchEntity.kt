package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.entity.BaseTimeEntity
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.type.MemberType
import jakarta.persistence.*

@Entity(name = "member_search")
class MemberSearchEntity(
    @Column(columnDefinition = "TEXT")
    var content: String,

    @Enumerated(EnumType.STRING)
    var language: LanguageType,

    @OneToOne
    @JoinColumn(name = "professor_id")
    val professor: ProfessorTranslationEntity? = null,

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: StaffTranslationEntity? = null
) : BaseTimeEntity() {
    companion object {
        fun create(professor: ProfessorTranslationEntity): MemberSearchEntity {
            return MemberSearchEntity(
                content = createContent(professor),
                language = professor.language,
                professor = professor
            )
        }

        fun create(staff: StaffTranslationEntity): MemberSearchEntity {
            return MemberSearchEntity(
                content = createContent(staff),
                language = staff.language,
                staff = staff
            )
        }

        fun createContent(translation: ProfessorTranslationEntity): String {
            val professor = translation.professor
            val stringBuilder = StringBuilder()
            stringBuilder.appendLine(translation.name)
            stringBuilder.appendLine(professor.status.krValue)
            stringBuilder.appendLine(translation.academicRank)
            stringBuilder.appendLine(translation.department)
            // 소속 연구실 이름은 같은 언어판으로.
            professor.lab?.translationOf(translation.language)?.let { stringBuilder.appendLine(it.name) }
            professor.startDate?.let { stringBuilder.appendLine(it) }
            professor.endDate?.let { stringBuilder.appendLine(it) }
            translation.office?.let { stringBuilder.appendLine(it) }
            professor.phone?.let { stringBuilder.appendLine(it) }
            professor.fax?.let { stringBuilder.appendLine(it) }
            professor.email?.let { stringBuilder.appendLine(it) }
            professor.website?.let { stringBuilder.appendLine(it) }
            translation.educations.forEach { stringBuilder.appendLine(it) }
            translation.researchAreas.forEach { stringBuilder.appendLine(it) }
            translation.careers.forEach { stringBuilder.appendLine(it) }

            return stringBuilder.toString()
        }

        fun createContent(staff: StaffTranslationEntity): String {
            val stringBuilder = StringBuilder()
            stringBuilder.appendLine(staff.name)
            stringBuilder.appendLine(staff.role)
            stringBuilder.appendLine(staff.office)
            stringBuilder.appendLine(staff.staff.phone)
            stringBuilder.appendLine(staff.staff.email)
            staff.tasks.forEach { stringBuilder.appendLine(it) }

            return stringBuilder.toString()
        }
    }

    @PrePersist
    @PreUpdate
    fun checkType() {
        if (
            (professor != null && staff != null) ||
            (professor == null && staff == null)
        ) {
            throw RuntimeException("MemberSearchEntity must have either professor or staff")
        }
    }

    fun ofType(): MemberType {
        return if (professor != null) {
            MemberType.PROFESSOR
        } else if (staff != null) {
            MemberType.STAFF
        } else {
            throw RuntimeException("MemberSearchEntity must have either professor or staff")
        }
    }

    fun update(professor: ProfessorTranslationEntity) {
        this.language = professor.language
        this.content = createContent(professor)
    }

    fun update(staff: StaffTranslationEntity) {
        this.language = staff.language
        this.content = createContent(staff)
    }
}

fun ProfessorTranslationEntity.syncSearch() {
    memberSearch?.update(this) ?: run { memberSearch = MemberSearchEntity.create(this) }
}

fun StaffTranslationEntity.syncSearch() {
    memberSearch?.update(this) ?: run { memberSearch = MemberSearchEntity.create(this) }
}
