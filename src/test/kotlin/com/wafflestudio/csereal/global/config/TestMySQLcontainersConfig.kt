package com.wafflestudio.csereal.global.config

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.MySQLContainer

object TestMySQLContainer {
    val instance = MySQLContainer<Nothing>("mysql:8.0").apply {
        withDatabaseName("testdb")
        withUsername("test")
        withPassword("test")
        withUrlParam("connectionTimeZone", "UTC")
    }
}

class TestContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        TestMySQLContainer.instance.start()

        TestPropertyValues.of(
            "spring.datasource.url=${TestMySQLContainer.instance.jdbcUrl}",
            "spring.datasource.username=${TestMySQLContainer.instance.username}",
            "spring.datasource.password=${TestMySQLContainer.instance.password}",
            "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
            "spring.jpa.database=mysql",
            "spring.jpa.database-platform=com.wafflestudio.csereal.common.config.MySQLDialectCustom",
            "spring.jpa.properties.hibernate.dialect=com.wafflestudio.csereal.common.config.MySQLDialectCustom",
            "spring.jpa.hibernate.ddl-auto=none",
            "spring.flyway.enabled=true",
            "spring.flyway.locations=classpath:db/migration"
        ).applyTo(applicationContext)
    }
}
