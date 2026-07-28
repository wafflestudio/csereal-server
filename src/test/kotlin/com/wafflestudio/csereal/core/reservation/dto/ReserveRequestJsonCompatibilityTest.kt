package com.wafflestudio.csereal.core.reservation.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class ReserveRequestJsonCompatibilityTest : StringSpec({
    "the application-configured ObjectMapper ignores a stale reservationType without policy effect" {
        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration::class.java))
            .run { context ->
                context.startupFailure shouldBe null
                val request = context.getBean(ObjectMapper::class.java).readValue<ReserveRequest>(
                    """
                    {
                      "roomId": 1,
                      "title": "legacy",
                      "contactEmail": "legacy@example.com",
                      "contactPhone": "010-0000-0000",
                      "professor": "professor",
                      "purpose": "meeting",
                      "startTime": "2027-03-10T10:00:00",
                      "endTime": "2027-03-10T11:00:00",
                      "agreed": true,
                      "recurringWeeks": 1,
                      "reservationType": "REGULAR"
                    }
                    """.trimIndent()
                )

                request.recurringWeeks shouldBe 1
                ReserveRequest::class.java.declaredFields.map { it.name }.contains("reservationType") shouldBe false
            }
    }
})
