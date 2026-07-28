package com.wafflestudio.csereal.core.reservation.config

import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated
import java.time.Clock
import java.time.ZoneId

@Validated
@ConfigurationProperties("csereal.reservation")
data class ReservationProperties(
    @field:Min(1)
    val maxRecurringWeeks: Int = 20
)

@Configuration
@EnableConfigurationProperties(ReservationProperties::class)
class ReservationConfig {
    @Bean
    fun reservationClock(): Clock = Clock.system(ZoneId.of("Asia/Seoul"))
}
