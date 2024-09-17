package com.wafflestudio.csereal.core.research.database

import com.querydsl.core.Tuple
import com.querydsl.jpa.impl.JPAQueryFactory
import com.wafflestudio.csereal.common.enums.LanguageType
import com.wafflestudio.csereal.core.research.database.QResearchLanguageEntity.researchLanguageEntity
import com.wafflestudio.csereal.core.research.type.ResearchRelatedType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

interface ResearchLanguageRepository : JpaRepository<ResearchLanguageEntity, Long>, ResearchLanguageCustomRepository {
    fun existsByKoreanIdAndEnglishIdAndType(koreanId: Long, englishId: Long, type: ResearchRelatedType): Boolean
    fun findByKoreanIdAndEnglishIdAndType(
        koreanId: Long,
        englishId: Long,
        type: ResearchRelatedType
    ): ResearchLanguageEntity?
}

interface ResearchLanguageCustomRepository {
    fun findResearchPairById(id: Long): Map<LanguageType, ResearchEntity>?
    fun findLabPairById(id: Long): Map<LanguageType, LabEntity>?
}

@Repository
class ResearchLanguageCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : ResearchLanguageCustomRepository {
    override fun findResearchPairById(id: Long): Map<LanguageType, ResearchEntity>? {
        val ko = QResearchEntity("ko")
        val en = QResearchEntity("en")

        val tuple = queryFactory.select(ko, en)
            .from(researchLanguageEntity)
            .join(ko).on(researchLanguageEntity.koreanId.eq(ko.id))
            .join(en).on(researchLanguageEntity.englishId.eq(en.id))
            .where(
                researchLanguageEntity.type.`in`(
                    listOf(
                        ResearchRelatedType.RESEARCH_GROUP,
                        ResearchRelatedType.RESEARCH_CENTER
                    )
                ),
                researchLanguageEntity.koreanId.eq(id).or(
                    researchLanguageEntity.englishId.eq(id)
                )
            ).fetchOne()

        return tuple?.let {
            mapOf(
                LanguageType.KO to it[ko]!!,
                LanguageType.EN to it[en]!!
            )
        }
    }

    override fun findLabPairById(id: Long): Map<LanguageType, LabEntity>? {
        val ko = QLabEntity("ko")
        val en = QLabEntity("en")

        val tuple: Tuple? = queryFactory.select(ko, en)
            .from(researchLanguageEntity)
            .join(ko).on(researchLanguageEntity.koreanId.eq(ko.id))
            .join(en).on(researchLanguageEntity.englishId.eq(en.id))
            .where(
                researchLanguageEntity.type.eq(
                    ResearchRelatedType.LAB,
                ),
                researchLanguageEntity.koreanId.eq(id).or(
                    researchLanguageEntity.englishId.eq(id)
                )
            ).fetchOne()

        return tuple?.let {
            mapOf(
                LanguageType.KO to it[ko]!!,
                LanguageType.EN to it[en]!!
            )
        }
    }
}
