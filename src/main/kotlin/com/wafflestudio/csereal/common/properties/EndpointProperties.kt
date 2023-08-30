package com.wafflestudio.csereal.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties("endpoint")
data class EndpointProperties(
    val frontend: String,
    val backend: String
)
