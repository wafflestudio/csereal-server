package com.wafflestudio.csereal.core.reservation.dto

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.wafflestudio.csereal.core.reservation.database.ReservationType
import com.wafflestudio.csereal.core.reservation.database.resolveRequestReservationType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ReserveRequestJsonCompatibilityTest : StringSpec({
    "legacy JSON without reservationType resolves to AD_HOC" {
        val objectMapper = jacksonObjectMapper().findAndRegisterModules()
        val request = objectMapper.readValue<ReserveRequest>(
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
              "recurringWeeks": 1
            }
            """.trimIndent()
        )

        request.reservationType shouldBe null
        resolveRequestReservationType(request.reservationType, request.recurringWeeks) shouldBe
            ReservationType.AD_HOC
    }
})
