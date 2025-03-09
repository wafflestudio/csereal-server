package com.wafflestudio.csereal.common.config

import com.wafflestudio.csereal.common.properties.EndpointProperties
import com.wafflestudio.csereal.core.user.service.CustomOidcUserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Profile("!test")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(EndpointProperties::class)
class SecurityConfig(
    private val customOidcUserService: CustomOidcUserService,
    private val endpointProperties: EndpointProperties,
    @Value("\${login-page}")
    private val loginPage: String
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .cors { }
            .csrf { it.disable() }
            .oauth2Login { oauth2 ->
                oauth2
                    .loginPage("$loginPage/oauth2/authorization/idsnucse")
                    .redirectionEndpoint { redirect ->
                        redirect.baseUri("/api/v1/login/oauth2/code/idsnucse")
                    }
                    .userInfoEndpoint { userInfo ->
                        userInfo.oidcUserService(customOidcUserService)
                    }
                    .successHandler(CustomAuthenticationSuccessHandler(endpointProperties.frontend))
            }
            .logout { logout ->
                logout
                    .logoutUrl("/api/v1/logout")
                    .logoutSuccessHandler(oidcLogoutSuccessHandler())
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/v1/login").authenticated()
                    .requestMatchers("/api/v2/admin/**").hasRole("STAFF")
                    .anyRequest().permitAll()
            }
            .headers { header ->
                header.referrerPolicy {
                    it.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                }
            }
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
        configuration.allowedOrigins = listOf(endpointProperties.frontend)
        configuration.allowedMethods = listOf("*")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3000
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
