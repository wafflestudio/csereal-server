package com.wafflestudio.csereal.core.member.database

import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.member.database.QCareerEntity.careerEntity
import com.wafflestudio.csereal.core.member.database.QEducationEntity.educationEntity
import com.wafflestudio.csereal.core.member.database.QMemberLanguageEntity.memberLanguageEntity
import com.wafflestudio.csereal.core.member.database.QProfessorEntity.professorEntity
import com.wafflestudio.csereal.core.member.database.QResearchAreaEntity.researchAreaEntity
import com.wafflestudio.csereal.core.member.type.MemberType
import com.wafflestudio.csereal.core.research.database.QLabEntity.labEntity
import com.wafflestudio.csereal.core.resource.mainImage.database.QMainImageEntity.mainImageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface ProfessorRepository : JpaRepository<ProfessorEntity, Long>, ProfessorRepositoryCustom {
    fun findByLanguageAndStatus(
        languageType: LanguageType, status: ProfessorStatus
    ): List<ProfessorEntity>

    fun findByLanguageAndStatusNot(
        languageType: LanguageType, status: ProfessorStatus
    ): List<ProfessorEntity>
}

interface ProfessorRepositoryCustom {
    fun findProfessorAllLanguages(id: Long): Map<LanguageType, List<ProfessorEntity>>
}

@Repository
class ProfessorRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
) : ProfessorRepositoryCustom {
    override fun findProfessorAllLanguages(id: Long): Map<LanguageType, List<ProfessorEntity>> {
        val professors = queryFactory.selectFrom(professorEntity)
            .where(
                professorEntity.id.`in`(
                    JPAExpressions.select(
                        memberLanguageEntity.koreanId
                    ).from(memberLanguageEntity)
                        .where(
                            memberLanguageEntity.englishId.eq(id),
                            memberLanguageEntity.type.eq(MemberType.PROFESSOR)
                        ),
                    JPAExpressions.select(
                        memberLanguageEntity.englishId
                    ).from(memberLanguageEntity)
                        .where(
                            memberLanguageEntity.koreanId.eq(id),
                            memberLanguageEntity.type.eq(MemberType.PROFESSOR)
                        ),
                    Expressions.constant(id)
                )
            ).leftJoin(mainImageEntity).fetchJoin()
            .leftJoin(labEntity).fetchJoin()
            .leftJoin(careerEntity).on(careerEntity.professor.eq(professorEntity))
            .leftJoin(researchAreaEntity).on(researchAreaEntity.professor.eq(professorEntity))
            .leftJoin(educationEntity).on(educationEntity.professor.eq(professorEntity))
            .fetch()

        return professors.groupBy {
            it.language
        }
    }
}

