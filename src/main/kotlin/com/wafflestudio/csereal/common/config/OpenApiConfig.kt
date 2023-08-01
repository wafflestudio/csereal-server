package com.wafflestudio.csereal.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openAPI(): OpenAPI {
        val info = Info()
                .title("컴퓨터공학부 홈페이지 백엔드 API")
                .description("컴퓨터공학부 홈페이지 백엔드 API 명세서입니다.")

        return OpenAPI()
                .components(Components())
                .info(info)
    }
}