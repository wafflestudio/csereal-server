package com.wafflestudio.csereal.common.config

import com.wafflestudio.csereal.common.ErrorCode
import com.wafflestudio.csereal.common.dto.ErrorResponse
import com.wafflestudio.csereal.common.properties.EndpointProperties
import com.wafflestudio.csereal.core.user.service.CustomOidcUserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler
import org.springframework.security.web.util.matcher.RequestMatcher
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
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
    private val clientRegistrationRepository: ObjectProvider<ClientRegistrationRepository>,
    @Value("\${login-page}")
    private val loginPage: String,
    private val objectMapper: ObjectMapper
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        clientRegistrationRepository.ifAvailable?.let {
            http.oauth2Login { oauth2 ->
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
        }

        return http
            .cors { }
            .csrf { it.disable() }
            // URL 규칙(authorizeHttpRequests)에서 막힌 API 요청은 로그인 페이지로 302 시키지 않고 ErrorResponse 로 답한다.
            // fetch 는 302 를 따라가다 외부 도메인에서 차단돼 네트워크 오류로 끝나므로, 프론트가 "로그인이 풀렸다"를 알 수 없다.
            //
            // 여기는 필터 체인이라 DispatcherServlet 에 닿기 전이고, 그래서 @RestControllerAdvice 가 볼 수 없다.
            // 메서드 단계(@PreAuthorize) 거부는 반대로 서블릿 안에서 나서 CserealExceptionHandler 가 잡는다 —
            // 둘은 중복이 아니라 서로 다른 지점을 막는다(실측 확인).
            //
            // /api/v1/login 은 브라우저 로그인 진입점이라 302 가 맞으므로 제외한다.
            .exceptionHandling { ex ->
                ex.defaultAuthenticationEntryPointFor(
                    { _, response, _ -> writeError(response, ErrorCode.UNAUTHENTICATED) },
                    apiRequestsExceptLogin
                )
                ex.accessDeniedHandler { _, response, _ -> writeError(response, ErrorCode.FORBIDDEN) }
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

    private val apiRequestsExceptLogin = RequestMatcher { request ->
        request.servletPath.startsWith("/api/") && request.servletPath != "/api/v1/login"
    }

    private fun writeError(response: HttpServletResponse, error: ErrorCode) {
        response.status = error.status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write(objectMapper.writeValueAsString(ErrorResponse(error)))
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
