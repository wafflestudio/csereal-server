package com.wafflestudio.csereal.core.reservation.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import java.time.LocalDateTime

class ReserveRequestJsonCompatibilityTest : StringSpec({
    "the configured ObjectMapper preserves UTC components with or without Z and ignores a stale type" {
        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration::class.java))
            .run { context ->
                context.startupFailure shouldBe null
                val objectMapper = context.getBean(ObjectMapper::class.java)
                val offsetFreeJson =
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
                val offsetFreeRequest = objectMapper.readValue<ReserveRequest>(offsetFreeJson)
                val utcRequest = objectMapper.readValue<ReserveRequest>(
                    offsetFreeJson
                        .replace("2027-03-10T10:00:00", "2027-03-10T10:00:00Z")
                        .replace("2027-03-10T11:00:00", "2027-03-10T11:00:00Z")
                )

                offsetFreeRequest.recurringWeeks shouldBe 1
                offsetFreeRequest.startTime shouldBe LocalDateTime.of(2027, 3, 10, 10, 0)
                offsetFreeRequest.endTime shouldBe LocalDateTime.of(2027, 3, 10, 11, 0)
                utcRequest.startTime shouldBe offsetFreeRequest.startTime
                utcRequest.endTime shouldBe offsetFreeRequest.endTime
                ReserveRequest::class.java.declaredFields.map { it.name }.contains("reservationType") shouldBe false
            }
    }
})
