package com.wafflestudio.csereal.common.mockauth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository

@Configuration
class MockAuthConfig {

    @Bean
    fun securityContextRepository(): SecurityContextRepository {
        return HttpSessionSecurityContextRepository()
    }
}
