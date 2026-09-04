package com.wafflestudio.csereal.common.config

import com.wafflestudio.csereal.common.dto.ErrorResponse
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openAPI(): OpenAPI {
        val info = Info()
            .title("컴퓨터공학부 홈페이지 백엔드 API")
            .description("컴퓨터공학부 홈페이지 백엔드 API 명세서입니다.")
            .version("1")

        return OpenAPI()
            .components(Components())
            .info(info)
    }

    /**
     * 오류 응답을 스펙에 싣는다. springdoc 은 @ControllerAdvice 의 응답을 자동으로 문서화하지 않으므로
     * 모든 오퍼레이션에 4XX/5XX → ErrorResponse 를 달고, 그 스키마(code enum 포함)를 components 에 넣는다.
     * 프론트는 이 enum 으로 오류 문구 사전의 완전성을 컴파일 시점에 검사한다.
     */
    @Bean
    fun errorResponseCustomizer() = OpenApiCustomizer { openApi ->
        val resolved = ModelConverters.getInstance().resolveAsResolvedSchema(AnnotatedType(ErrorResponse::class.java))
        resolved.referencedSchemas.forEach { (name, schema) -> openApi.components.addSchemas(name, schema) }

        val errorContent = Content().addMediaType(
            org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
            MediaType().schema(Schema<Any>().`$ref`("#/components/schemas/ErrorResponse"))
        )
        openApi.paths?.values?.flatMap { it.readOperations() }?.forEach { op ->
            op.responses.addApiResponse("4XX", ApiResponse().description("요청 오류 — code 로 구분").content(errorContent))
            op.responses.addApiResponse("5XX", ApiResponse().description("서버 오류").content(errorContent))
        }
    }
}
