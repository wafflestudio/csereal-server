package com.wafflestudio.csereal.common.interceptor

import com.wafflestudio.csereal.common.context.ClientInfoHolder
import com.wafflestudio.csereal.common.dto.ClientInfo
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.HandlerInterceptor

private const val CLIENT_INFO_HEADER = "X-Client-Id"
private const val FORWARDED_FOR_HEADER = "X-Forwarded-For"

@Configuration
class ClientInfoInterceptor(
    private val clientInfoHolder: ClientInfoHolder
) : HandlerInterceptor {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val ipAddress: String = request.getHeader(FORWARDED_FOR_HEADER)?.let { xff ->
            xff.split(",").map { it.trim() }.firstOrNull()
        } ?: request.remoteAddr
        val clientId: String? = request.getHeader(CLIENT_INFO_HEADER)

        val clientInfo = ClientInfo(ipAddress, clientId)
        logger.info("client info: {}", clientInfo)

        // since only ip address can be used, we set the clientInfo even if it is invalid (no clientId)
        clientInfoHolder.clientInfo = clientInfo

        return true
    }
}
