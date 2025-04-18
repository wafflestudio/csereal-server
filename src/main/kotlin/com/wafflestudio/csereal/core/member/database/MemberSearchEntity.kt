package com.wafflestudio.csereal.core.member.database

import com.wafflestudio.csereal.common.config.BaseTimeEntity
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.type.MemberType
import jakarta.persistence.*

@Entity(name = "member_search")
class MemberSearchEntity(
    @Column(columnDefinition = "TEXT")
    var content: String,

    var language: LanguageType,

    @OneToOne
    @JoinColumn(name = "professor_id")
    val professor: ProfessorEntity? = null,

    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: StaffEntity? = null
) : BaseTimeEntity() {
    companion object {
        fun create(professor: ProfessorEntity): MemberSearchEntity {
            return MemberSearchEntity(
                content = createContent(professor),
                language = professor.language,
                professor = professor
            )
        }

        fun create(staff: StaffEntity): MemberSearchEntity {
            return MemberSearchEntity(
                content = createContent(staff),
                language = staff.language,
                staff = staff
            )
        }

        fun createContent(professor: ProfessorEntity): String {
            val stringBuilder = StringBuilder()
            stringBuilder.appendLine(professor.name)
            stringBuilder.appendLine(professor.status.krValue)
            stringBuilder.appendLine(professor.academicRank)
            professor.lab?.let { stringBuilder.appendLine(it.name) }
            professor.startDate?.let { stringBuilder.appendLine(it) }
            professor.endDate?.let { stringBuilder.appendLine(it) }
            professor.office?.let { stringBuilder.appendLine(it) }
            professor.phone?.let { stringBuilder.appendLine(it) }
            professor.fax?.let { stringBuilder.appendLine(it) }
            professor.email?.let { stringBuilder.appendLine(it) }
            professor.website?.let { stringBuilder.appendLine(it) }
            professor.educations.forEach { stringBuilder.appendLine(it) }
            professor.researchAreas.forEach { stringBuilder.appendLine(it) }
            professor.careers.forEach { stringBuilder.appendLine(it) }

            return stringBuilder.toString()
        }

        fun createContent(staff: StaffEntity): String {
            val stringBuilder = StringBuilder()
            stringBuilder.appendLine(staff.name)
            stringBuilder.appendLine(staff.role)
            stringBuilder.appendLine(staff.office)
            stringBuilder.appendLine(staff.phone)
            stringBuilder.appendLine(staff.email)
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

    fun update(professor: ProfessorEntity) {
        this.language = professor.language
        this.content = createContent(professor)
    }

    fun update(staff: StaffEntity) {
        this.language = staff.language
        this.content = createContent(staff)
    }
}
