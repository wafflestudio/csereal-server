package com.wafflestudio.csereal.core.member.database

import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.database.QMemberLanguageEntity.memberLanguageEntity
import com.wafflestudio.csereal.core.member.database.QStaffEntity.staffEntity
import com.wafflestudio.csereal.core.member.database.QTaskEntity.taskEntity
import com.wafflestudio.csereal.core.member.type.MemberType
import com.wafflestudio.csereal.core.resource.mainImage.database.QMainImageEntity.mainImageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface StaffRepository : JpaRepository<StaffEntity, Long>, StaffRepositoryCustom {
    fun findAllByLanguage(languageType: LanguageType): List<StaffEntity>
}

interface StaffRepositoryCustom {
    fun findStaffAllLanguages(id: Long): Map<LanguageType, List<StaffEntity>>
}

@Repository
class StaffRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) : StaffRepositoryCustom {
    override fun findStaffAllLanguages(id: Long): Map<LanguageType, List<StaffEntity>> {
        val staffs = queryFactory.selectFrom(staffEntity)
            .where(
                staffEntity.id.`in`(
                    JPAExpressions.select(
                        memberLanguageEntity.koreanId
                    ).from(memberLanguageEntity)
                        .where(memberLanguageEntity.englishId.eq(id), memberLanguageEntity.type.eq(MemberType.STAFF)),
                    JPAExpressions.select(
                        memberLanguageEntity.englishId
                    ).from(memberLanguageEntity)
                        .where(memberLanguageEntity.koreanId.eq(id), memberLanguageEntity.type.eq(MemberType.STAFF)),
                    Expressions.constant(id)
                )
            ).leftJoin(mainImageEntity).fetchJoin()
            .leftJoin(taskEntity).on(taskEntity.staff.eq(staffEntity))
            .fetch()

        return staffs.groupBy {
            it.language
        }
    }
}
