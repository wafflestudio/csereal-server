package com.wafflestudio.csereal.common.config

import com.wafflestudio.csereal.core.user.service.CustomOidcUserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val customOidcUserService: CustomOidcUserService
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .cors().and()
            .csrf().disable()
            .oauth2Login()
            .loginPage("/oauth2/authorization/idsnucse")
            .userInfoEndpoint().oidcUserService(customOidcUserService).and()
            .successHandler(CustomAuthenticationSuccessHandler()).and()
            .logout()
            .logoutSuccessHandler(oidcLogoutSuccessHandler())
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID").and()
            .authorizeHttpRequests()
            .requestMatchers("/login").authenticated()
            .anyRequest().permitAll().and()
            .build()
    }

    @Bean
    fun oidcLogoutSuccessHandler(): LogoutSuccessHandler {
        return object : SimpleUrlLogoutSuccessHandler() {
            override fun onLogoutSuccess(
                request: HttpServletRequest?,
                response: HttpServletResponse?,
                authentication: Authentication?
            ) {
                super.setDefaultTargetUrl("http://cse-dev-waffle.bacchus.io:3000/")
                super.onLogoutSuccess(request, response, authentication)
            }
        }
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("*")
        configuration.allowedMethods = listOf("*")
        configuration.allowedHeaders = listOf("*")
        configuration.maxAge = 3000
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

}
