package com.wafflestudio.csereal.common.util

import jakarta.persistence.EntityManager
import jakarta.persistence.Table
import jakarta.transaction.Transactional
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Profile("test")
@Component
class CleanUp(
    private var jdbcTemplate: JdbcTemplate,
    private var entityManager: EntityManager
) {
    private fun camelToSnake(camelCase: String): String {
        return camelCase.replace(Regex("([a-z])([A-Z])"), "$1_$2")
            .lowercase(Locale.getDefault())
    }

    @Transactional
    fun all() {
        val tables = entityManager.metamodel.entities.mapNotNull { entityType ->
            val clazz = entityType.javaType
            val tableAnnotation = clazz.getAnnotation(Table::class.java)
            when {
                tableAnnotation != null && tableAnnotation.name.isNotBlank() -> tableAnnotation.name
                else -> entityType.name // fallback to entity name
            }
        }

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0")

        tables.forEach { table ->
            val tableNameInSnakeCase = camelToSnake(table) // 테이블 이름을 Snake_case로 변환
            try {
                jdbcTemplate.execute("TRUNCATE TABLE `$tableNameInSnakeCase`") // 변환된 이름 사용
            } catch (e: Exception) {
                println("Failed to truncate table $tableNameInSnakeCase: ${e.message}")
            }
        }

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1")
    }
}
