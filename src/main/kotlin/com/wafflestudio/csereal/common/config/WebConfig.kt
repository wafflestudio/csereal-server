package com.wafflestudio.csereal.common.config

import com.wafflestudio.csereal.common.interceptor.ClientInfoInterceptor
import com.wafflestudio.csereal.common.properties.EndpointProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableConfigurationProperties(EndpointProperties::class)
class WebConfig(
    private val endpointProperties: EndpointProperties,
    private val clientInfoInterceptor: ClientInfoInterceptor
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(endpointProperties.frontend)
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3000)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(clientInfoInterceptor)
    }
}
