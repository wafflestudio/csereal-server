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
import org.springframework.web.client.RestTemplate


@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val customOidcUserService: CustomOidcUserService
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http.csrf().disable()
            .oauth2Login()
            .loginPage("/oauth2/authorization/idsnucse")
            .userInfoEndpoint().oidcUserService(customOidcUserService).and()
            .and()
            .logout()
            .logoutSuccessHandler(oidcLogoutSuccessHandler())
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID")
            .and()
            .authorizeHttpRequests()
            .requestMatchers("/login").authenticated()
            .anyRequest().permitAll()
            .and()
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
                super.setDefaultTargetUrl("/")
                super.onLogoutSuccess(request, response, authentication)
            }
        }
    }

}
