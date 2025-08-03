package com.wafflestudio.csereal.common.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.servlet.HandlerInterceptor
import java.net.InetAddress
import java.util.UUID

private const val CLIENT_INFO_HEADER = "X-Client-Id"
private const val FORWARDED_FOR_HEADER = "X-Forwarded-For"

@Configuration
class ClientInfoInterceptor(
    private val clientInfo: ClientInfo
) : HandlerInterceptor {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val ipAddress: String = request.getHeader(FORWARDED_FOR_HEADER)?.let { xff ->
            xff.split(",").map { it.trim() }.firstOrNull()
        } ?: request.remoteAddr
        val clientId: String? = request.getHeader(CLIENT_INFO_HEADER)

        clientInfo.apply {
            this.setIpAddress(ipAddress)
            this.setClientId(clientId)
        }

        logger.info("client info: {}", clientInfo)

        return true
    }
}

@Component
@RequestScope
data class ClientInfo(
    var ipAddress: InetAddress? = null,
    var clientId: UUID? = null
) {
    constructor(ipAddress: String, clientId: String?) : this() {
        this.setIpAddress(ipAddress)
        this.setClientId(clientId)
    }

    fun setIpAddress(ipAddress: String) {
        try {
            this.ipAddress = InetAddress.getByName(ipAddress)
        } catch (e: Exception) {
            this.ipAddress = null
        }
    }

    fun setClientId(userId: String?) {
        try {
            this.clientId = userId?.let {
                UUID.fromString(it)
            }
        } catch (e: Exception) {
            this.clientId = null
        }
    }

    fun isValid() = ipAddress != null && clientId != null
}
