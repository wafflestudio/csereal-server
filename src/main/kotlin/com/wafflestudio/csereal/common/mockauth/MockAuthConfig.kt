package com.wafflestudio.csereal.common.mockauth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository

@Configuration
class MockAuthConfig(
    private val devAuthenticationProvider: DevAuthenticationProvider
) {
    @Bean
    fun authenticationManager(http: HttpSecurity): AuthenticationManager {
        http.authenticationProvider(devAuthenticationProvider)
        return http.getSharedObject(AuthenticationManagerBuilder::class.java).build()
    }

    @Bean
    fun securityContextRepository(): SecurityContextRepository {
        return HttpSessionSecurityContextRepository()
    }
}
