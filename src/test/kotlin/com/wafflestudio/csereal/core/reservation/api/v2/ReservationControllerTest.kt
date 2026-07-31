package com.wafflestudio.csereal.core.reservation.api.v2

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wafflestudio.csereal.common.CserealException
import com.wafflestudio.csereal.common.config.CserealExceptionHandler
import com.wafflestudio.csereal.common.config.LocalDateTimeSerializer
import com.wafflestudio.csereal.core.reservation.database.ReserveTermEntity
import com.wafflestudio.csereal.core.reservation.database.ReserveTermType
import com.wafflestudio.csereal.core.reservation.dto.CreateCustomReserveTermRequest
import com.wafflestudio.csereal.core.reservation.dto.ReserveTermDto
import com.wafflestudio.csereal.core.reservation.service.ReservationService
import com.wafflestudio.csereal.core.reservation.service.ReserveTermDescriptor
import com.wafflestudio.csereal.core.reservation.service.ReserveTermGenerationOutcome
import com.wafflestudio.csereal.core.reservation.service.ReserveTermGenerationResult
import com.wafflestudio.csereal.core.reservation.service.ReserveTermGenerationService
import com.wafflestudio.csereal.core.reservation.service.ReserveTermManualCreationService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDateTime

class ReservationControllerTest : StringSpec({
    val reservationService = mockk<ReservationService>()
    val manualCreationService = mockk<ReserveTermManualCreationService>()
    val generationService = mockk<ReserveTermGenerationService>()
    val localDateTimeModule = SimpleModule().apply {
        addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer())
    }
    val objectMapper = jacksonObjectMapper()
        .findAndRegisterModules()
        .registerModule(localDateTimeModule)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(
        ReservationController(reservationService, manualCreationService, generationService)
    )
        .setControllerAdvice(CserealExceptionHandler())
        .setMessageConverters(MappingJackson2HttpMessageConverter(objectMapper))
        .build()

    beforeTest {
        clearMocks(reservationService, manualCreationService, generationService)
    }

    "POST custom returns 201 through the JSON boundary" {
        val requestSlot = slot<CreateCustomReserveTermRequest>()
        every { manualCreationService.createCustom(capture(requestSlot)) } returns customEntity()

        mockMvc.perform(
            post(CUSTOM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestJson)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(0))
            .andExpect(jsonPath("$.applyStartTime").value("2027-02-01T00:00:00Z"))
            .andExpect(jsonPath("$.applyEndTime").value("2027-02-28T15:00:00Z"))
            .andExpect(jsonPath("$.termStartTime").value("2027-02-28T15:00:00Z"))
            .andExpect(jsonPath("$.termEndTime").value("2027-06-30T15:00:00Z"))

        verify(exactly = 1) { manualCreationService.createCustom(any()) }
        requestSlot.captured shouldBe customRequest
    }

    listOf(
        "no body" to null,
        "a missing field" to validRequestJson.replace(",\"termEndTime\":\"2027-06-30T15:00:00\"", ""),
        "a null field" to validRequestJson.replace(
            "\"termEndTime\":\"2027-06-30T15:00:00\"",
            "\"termEndTime\":null"
        ),
        "malformed JSON" to "{",
        "a malformed date-time" to validRequestJson.replace("2027-02-01T00:00:00", "not-a-date")
    ).forEach { (case, body) ->
        "POST custom rejects $case before service invocation" {
            val request = post(CUSTOM_PATH).contentType(MediaType.APPLICATION_JSON)
            if (body != null) request.content(body)

            mockMvc.perform(request).andExpect(status().isBadRequest)

            verify(exactly = 0) { manualCreationService.createCustom(any()) }
        }
    }

    "POST custom maps an invalid schedule to 400" {
        every { manualCreationService.createCustom(any()) } throws CserealException.Csereal400("invalid_schedule")

        mockMvc.perform(
            post(CUSTOM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestJson)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("invalid_schedule"))

        verify(exactly = 1) { manualCreationService.createCustom(any()) }
    }

    "POST custom maps an overlap to 409" {
        every { manualCreationService.createCustom(any()) } throws CserealException.Csereal409("reserve_term_overlap")

        mockMvc.perform(
            post(CUSTOM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequestJson)
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.message").value("reserve_term_overlap"))

        verify(exactly = 1) { manualCreationService.createCustom(any()) }
    }

    "POST defaults returns exact sanitized JSON for both outcomes" {
        every { generationService.ensureCurrentAndNext() } returns listOf(
            ReserveTermGenerationOutcome(
                descriptor(2027, ReserveTermType.FIRST_SEMESTER),
                ReserveTermGenerationResult.FAILED,
                IllegalStateException("database details")
            ),
            ReserveTermGenerationOutcome(
                descriptor(2027, ReserveTermType.WINTER),
                ReserveTermGenerationResult.CREATED
            )
        )

        val expected = """[
            {"termYear":2027,"termType":"FIRST_SEMESTER","result":"FAILED","reason":"generation_failed"},
            {"termYear":2027,"termType":"WINTER","result":"CREATED","reason":null}
        ]
        """.trimIndent()
        mockMvc.perform(post(DEFAULTS_PATH))
            .andExpect(status().isOk)
            .andExpect(content().json(expected, true))
            .andExpect(jsonPath("$[0].error").doesNotExist())
            .andExpect(jsonPath("$[0].message").doesNotExist())
            .andExpect(jsonPath("$[0].descriptor").doesNotExist())
            .andExpect(jsonPath("$[0].candidates").doesNotExist())

        verify(exactly = 1) { generationService.ensureCurrentAndNext() }
    }

    "GET terms keeps the existing public response boundary" {
        val term = ReserveTermDto(
            id = 17,
            applyStartTime = customRequest.applyStartTime,
            applyEndTime = customRequest.applyEndTime,
            termStartTime = customRequest.termStartTime,
            termEndTime = customRequest.termEndTime
        )
        every { reservationService.getReserveTerms() } returns listOf(term)

        mockMvc.perform(get("/api/v2/reservation/terms"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(17))
            .andExpect(jsonPath("$[0].applyStartTime").value("2027-02-01T00:00:00Z"))
            .andExpect(jsonPath("$[0].applyEndTime").value("2027-02-28T15:00:00Z"))
            .andExpect(jsonPath("$[0].termStartTime").value("2027-02-28T15:00:00Z"))
            .andExpect(jsonPath("$[0].termEndTime").value("2027-06-30T15:00:00Z"))

        verify(exactly = 1) { reservationService.getReserveTerms() }
    }
}) {
    companion object {
        private const val CUSTOM_PATH = "/api/v2/reservation/terms/custom"
        private const val DEFAULTS_PATH = "/api/v2/reservation/terms/defaults"
        private val validRequestJson = """{
            "applyStartTime":"2027-02-01T00:00:00",
            "applyEndTime":"2027-02-28T15:00:00",
            "termStartTime":"2027-02-28T15:00:00",
            "termEndTime":"2027-06-30T15:00:00"
        }
        """.trimIndent().replace("\n", "").replace(" ", "")

        private val customRequest = CreateCustomReserveTermRequest(
            LocalDateTime.of(2027, 2, 1, 0, 0),
            LocalDateTime.of(2027, 2, 28, 15, 0),
            LocalDateTime.of(2027, 2, 28, 15, 0),
            LocalDateTime.of(2027, 6, 30, 15, 0)
        )

        private fun customEntity() = ReserveTermEntity(
            customRequest.applyStartTime,
            customRequest.applyEndTime,
            customRequest.termStartTime,
            customRequest.termEndTime
        )

        private fun descriptor(year: Int, type: ReserveTermType) = ReserveTermDescriptor(
            year,
            type,
            LocalDateTime.of(year, 1, 1, 0, 0),
            LocalDateTime.of(year, 1, 14, 15, 0),
            LocalDateTime.of(year, 1, 31, 15, 0),
            LocalDateTime.of(year, 2, 28, 15, 0)
        )
    }
}
