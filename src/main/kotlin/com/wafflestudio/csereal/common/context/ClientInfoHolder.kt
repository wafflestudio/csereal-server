package com.wafflestudio.csereal.common.context

import com.wafflestudio.csereal.common.dto.ClientInfo
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

/**
 * Provides access to client information within the lifetime of a single HTTP request.
 *
 * Implementations are expected to be request-scoped so that each request receives
 * its own instance and associated [ClientInfo].
 */
interface ClientInfoContext {
    val clientInfo: ClientInfo
}

/**
 * Request-scoped holder that stores client information for the current request.
 *
 * This concrete implementation is intended to be populated by an interceptor
 * (e.g., [com.wafflestudio.csereal.common.interceptor.ClientInfoInterceptor])
 * before controller logic executes.
 */
@Component
@RequestScope
class ClientInfoHolder : ClientInfoContext {
    /**
     * Client information captured for the current request.
     *
     * This property is initialized by the request processing pipeline, typically
     * in [com.wafflestudio.csereal.common.interceptor.ClientInfoInterceptor].
     */
    override lateinit var clientInfo: ClientInfo
}
