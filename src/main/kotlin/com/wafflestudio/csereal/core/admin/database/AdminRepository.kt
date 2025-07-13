package com.wafflestudio.csereal.core.admin.database

import com.wafflestudio.csereal.core.admin.dto.AdminImportantElement
import jakarta.persistence.EntityManagerFactory
import org.springframework.stereotype.Repository
import java.sql.Timestamp

@Repository
class AdminRepository(
    private val emf: EntityManagerFactory
) {
    fun readImportantsPagination(pageSize: Int, offset: Int): List<AdminImportantElement> {
        val em = emf.createEntityManager()
        val query = em.createNativeQuery(
            """
            (
                SELECT id, title, created_at, 'notice' AS type
                FROM notice
                WHERE is_important = TRUE
                UNION ALL
                SELECT news.id, title, date, 'news' AS type
                FROM news
                WHERE is_important = TRUE
                UNION ALL
                SELECT seminar.id, title, created_at, 'seminar' AS type
                FROM seminar
                WHERE is_important = TRUE
            ) ORDER BY created_at DESC
            LIMIT :pageSize OFFSET :offset
            """.trimIndent()
        )
        query.setParameter("pageSize", pageSize)
        query.setParameter("offset", offset)

        val result = query.resultList as List<Array<Any>>
        val formattedResult = result.map {
            AdminImportantElement(
                id = it[0] as Long,
                title = it[1] as String,
                createdAt = (it[2] as Timestamp).toLocalDateTime(),
                category = it[3] as String
            )
        }
        em.close()
        return formattedResult
    }

    fun getTotalImportantsCnt(): Long {
        val em = emf.createEntityManager()
        val query = em.createNativeQuery(
            """
            SELECT COUNT(*) FROM (
                SELECT id, title, created_at, 'notice' AS type
                FROM notice
                WHERE is_important = TRUE
                UNION ALL
                SELECT news.id, title, created_at, 'news' AS type
                FROM news
                WHERE is_important = TRUE
                UNION ALL
                SELECT seminar.id, title, created_at, 'seminar' AS type
                FROM seminar
                WHERE is_important = TRUE
            ) as nn
            """.trimIndent()
        )
        val total = query.resultList.first() as Long
        em.close()
        return total
    }
}
