package com.wafflestudio.csereal.global.config

import org.springframework.boot.test.context.TestConfiguration
import org.testcontainers.containers.MySQLContainer
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean

@TestConfiguration(proxyBeanMethods = false)
class MySQLTestContainerConfig {
    @Bean
    @ServiceConnection
    fun mySQLContainer(): MySQLContainer<Nothing> {
        return MySQLContainer<Nothing>("mysql:8.0").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
            withUrlParam("connectionTimeZone", "UTC")
        }
    }
}
