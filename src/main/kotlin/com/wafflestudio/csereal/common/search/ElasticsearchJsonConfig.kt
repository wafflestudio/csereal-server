package com.wafflestudio.csereal.common.search

import co.elastic.clients.json.jackson.JacksonJsonpMapper
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ElasticsearchJsonConfig {
    /**
     * 기본 JacksonJsonpMapper 는 Kotlin 모듈이 없는 ObjectMapper 를 쓴다.
     * 그러면 data class 를 되읽을 때 생성자를 못 찾아 검색 응답 파싱이 통째로 실패한다.
     * 앱이 쓰는 ObjectMapper 를 넘겨 색인·조회가 API 와 같은 규칙을 따르게 한다.
     */
    @Bean
    fun jacksonJsonpMapper(objectMapper: ObjectMapper) = JacksonJsonpMapper(objectMapper)
}
