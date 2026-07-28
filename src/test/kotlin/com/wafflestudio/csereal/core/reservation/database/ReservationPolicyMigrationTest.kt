package com.wafflestudio.csereal.core.reservation.database

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion
import org.testcontainers.containers.MySQLContainer
import java.sql.DriverManager
import java.sql.SQLException

class ReservationPolicyMigrationTest : FunSpec({
    val mysql = MySQLContainer<Nothing>("mysql:8.0").apply {
        withDatabaseName("reservation_policy_migration")
        withUsername("test")
        withPassword("test")
        withUrlParam("connectionTimeZone", "UTC")
    }
    val freshMysql = MySQLContainer<Nothing>("mysql:8.0").apply {
        withDatabaseName("reservation_policy_fresh")
        withUsername("test")
        withPassword("test")
        withUrlParam("connectionTimeZone", "UTC")
    }

    beforeSpec {
        mysql.start()
        try {
            freshMysql.start()
        } catch (exception: Exception) {
            mysql.stop()
            throw exception
        }
    }

    afterSpec {
        try {
            freshMysql.stop()
        } finally {
            mysql.stop()
        }
    }

    test("V16 preserves legacy reservation types as null and preserves unclassified reserve terms") {
        val v15Flyway = Flyway.configure()
            .dataSource(mysql.jdbcUrl, mysql.username, mysql.password)
            .locations("classpath:db/migration")
            .target(MigrationVersion.fromVersion("15"))
            .load()
        v15Flyway.migrate()

        DriverManager.getConnection(mysql.jdbcUrl, mysql.username, mysql.password).use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(
                    """
                    INSERT INTO reservation (recurring_weeks, agreed)
                    VALUES (1, b'1'), (4, b'1'), (0, b'1'), (-1, b'1')
                    """.trimIndent()
                )
                statement.executeUpdate(
                    """
                    INSERT INTO reserve_term (
                        created_at,
                        modified_at,
                        apply_start_time,
                        apply_end_time,
                        term_start_time,
                        term_end_time
                    ) VALUES
                        (NOW(6), NOW(6), '2027-02-01 09:00:00', '2027-07-01 00:00:00',
                         '2027-03-01 00:00:00', '2027-07-01 00:00:00'),
                        (NOW(6), NOW(6), '2027-02-10 09:00:00', '2027-06-20 00:00:00',
                         '2027-03-10 00:00:00', '2027-06-20 00:00:00')
                    """.trimIndent()
                )
            }
        }

        val latestFlyway = Flyway.configure()
            .dataSource(mysql.jdbcUrl, mysql.username, mysql.password)
            .locations("classpath:db/migration")
            .load()
        latestFlyway.migrate()

        DriverManager.getConnection(mysql.jdbcUrl, mysql.username, mysql.password).use { connection ->
            val reservationTypes: List<String?> = connection.createStatement().use { statement ->
                statement.executeQuery(
                    "SELECT reservation_type FROM reservation ORDER BY id"
                ).use { resultSet ->
                    buildList<String?> {
                        while (resultSet.next()) {
                            add(resultSet.getString("reservation_type"))
                        }
                    }
                }
            }
            reservationTypes.shouldContainExactly(null, null, null, null)

            connection.createStatement().use { statement ->
                statement.executeQuery(
                    """
                    SELECT COUNT(*) AS unclassified_count
                    FROM reserve_term
                    WHERE term_year IS NULL AND term_type IS NULL
                    """.trimIndent()
                ).use { resultSet ->
                    resultSet.next() shouldBe true
                    resultSet.getInt("unclassified_count") shouldBe 2
                }

                statement.executeUpdate(termInsert(2028, "FIRST_SEMESTER")) shouldBe 1
                shouldThrow<SQLException> {
                    statement.executeUpdate(termInsert(2029, null))
                }
                shouldThrow<SQLException> {
                    statement.executeUpdate(termInsert(2028, "FIRST_SEMESTER"))
                }
            }
        }
    }

    test("fresh V16 enforces the optional metadata pair") {
        Flyway.configure()
            .dataSource(freshMysql.jdbcUrl, freshMysql.username, freshMysql.password)
            .locations("classpath:db/migration")
            .load()
            .migrate()

        DriverManager.getConnection(freshMysql.jdbcUrl, freshMysql.username, freshMysql.password).use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(termInsert(null, null)) shouldBe 1
                statement.executeUpdate(termInsert(2030, "SUMMER")) shouldBe 1
                shouldThrow<SQLException> {
                    statement.executeUpdate(termInsert(null, "SUMMER"))
                }
            }
        }
    }
}) {
    companion object {
        private fun termInsert(termYear: Int?, termType: String?): String {
            val year = termYear?.toString() ?: "NULL"
            val type = termType?.let { "'$it'" } ?: "NULL"
            return """
                INSERT INTO reserve_term (
                    created_at, modified_at, apply_start_time, apply_end_time,
                    term_start_time, term_end_time, term_year, term_type
                ) VALUES (
                    NOW(6), NOW(6), '2030-01-01 09:00:00', '2030-01-15 00:00:00',
                    '2030-02-01 00:00:00', '2030-03-01 00:00:00', $year, $type
                )
            """.trimIndent()
        }
    }
}
