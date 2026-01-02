package com.wafflestudio.csereal.common.interceptor

import com.wafflestudio.csereal.common.context.ClientInfoHolder
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.UUID

class ClientInfoInterceptorTest : BehaviorSpec({

    Given("ClientInfoInterceptor with a fresh request-scoped holder") {
        val clientInfoHolder = ClientInfoHolder()
        val interceptor = ClientInfoInterceptor(clientInfoHolder)

        When("X-Forwarded-For contains multiple IPs and X-Client-Id is a valid UUID") {
            val request = mockk<HttpServletRequest>()
            val response = mockk<HttpServletResponse>(relaxed = true)
            val validUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")

            every { request.getHeader("X-Forwarded-For") } returns "203.0.113.7, 70.41.3.18, 150.172.238.178"
            every { request.getHeader("X-Client-Id") } returns validUuid.toString()
            every { request.remoteAddr } returns "198.51.100.23" // should be ignored due to XFF

            interceptor.preHandle(request, response, Any()) shouldBe true

            Then("it should pick the first X-Forwarded-For IP and parse the UUID") {
                clientInfoHolder.clientInfo.ipAddress.hostAddress shouldBe "203.0.113.7"
                clientInfoHolder.clientInfo.clientId shouldBe validUuid
                clientInfoHolder.clientInfo.isValid().shouldBeTrue()
            }
        }

        When("X-Forwarded-For is missing and X-Client-Id header is missing") {
            val request = mockk<HttpServletRequest>()
            val response = mockk<HttpServletResponse>(relaxed = true)

            every { request.getHeader("X-Forwarded-For") } returns null
            every { request.getHeader("X-Client-Id") } returns null
            every { request.remoteAddr } returns "192.0.2.1"

            interceptor.preHandle(request, response, Any()) shouldBe true

            Then("it should fallback to remoteAddr and have no clientId") {
                clientInfoHolder.clientInfo.ipAddress.hostAddress shouldBe "192.0.2.1"
                clientInfoHolder.clientInfo.clientId.shouldBeNull()
                clientInfoHolder.clientInfo.isValid().shouldBeFalse()
            }
        }

        When("X-Client-Id is present but invalid UUID") {
            val request = mockk<HttpServletRequest>()
            val response = mockk<HttpServletResponse>(relaxed = true)

            every { request.getHeader("X-Forwarded-For") } returns "2001:db8::1, 203.0.113.9"
            every { request.getHeader("X-Client-Id") } returns "not-a-uuid"
            every { request.remoteAddr } returns "192.0.2.55"

            interceptor.preHandle(request, response, Any()) shouldBe true

            Then("it should ignore invalid UUID and still capture IP from XFF") {
                // normalize IPv6 representation because InetAddress may compress zeros
                val captured = clientInfoHolder.clientInfo.ipAddress.hostAddress
                (captured == "2001:db8:0:0:0:0:0:1" || captured == "2001:db8::1") shouldBe true
                clientInfoHolder.clientInfo.clientId.shouldBeNull()
                clientInfoHolder.clientInfo.isValid().shouldBeFalse()
            }
        }
    }
})
