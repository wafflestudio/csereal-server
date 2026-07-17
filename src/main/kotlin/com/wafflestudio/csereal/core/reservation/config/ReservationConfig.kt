package com.wafflestudio.csereal.core.reservation.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.ZoneId

@Configuration
class ReservationConfig {
    @Bean
    fun reservationClock(): Clock = Clock.system(ZoneId.of("Asia/Seoul"))
}
