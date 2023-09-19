package com.wafflestudio.csereal

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class CserealApplication

fun main(args: Array<String>) {
    runApplication<CserealApplication>(*args)
}
