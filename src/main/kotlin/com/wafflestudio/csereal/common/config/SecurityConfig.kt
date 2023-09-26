package com.wafflestudio.csereal.common.config

import com.wafflestudio.csereal.common.properties.EndpointProperties
import com.wafflestudio.csereal.core.user.service.CustomOidcUserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.context.properties.EnableConfigurationProperties
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
@EnableConfigurationProperties(EndpointProperties::class)
class SecurityConfig(
    private val customOidcUserService: CustomOidcUserService,
    private val endpointProperties: EndpointProperties
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .cors().and()
            .csrf().disable()
            .oauth2Login()
            .loginPage("/oauth2/authorization/idsnucse")
            .redirectionEndpoint()
            .baseUri("/api/v1/login/oauth2/code/idsnucse").and()
            .userInfoEndpoint().oidcUserService(customOidcUserService).and()
            .successHandler(CustomAuthenticationSuccessHandler(endpointProperties.frontend)).and()
            .logout()
            .logoutUrl("/api/v1/logout")
            .logoutSuccessHandler(oidcLogoutSuccessHandler())
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID").and()
            .authorizeHttpRequests()
            .requestMatchers("/api/v1/login").authenticated()
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
                val redirectUrl = "${endpointProperties.frontend}/logout/success"
                super.setDefaultTargetUrl(redirectUrl)
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
