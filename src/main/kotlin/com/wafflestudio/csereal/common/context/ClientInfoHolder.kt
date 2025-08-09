package com.wafflestudio.csereal.common.context

import com.wafflestudio.csereal.common.dto.ClientInfo
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

interface ClientInfoContext {
    fun getClientInfo(): ClientInfo
}

@Component
@RequestScope
class ClientInfoHolder : ClientInfoContext {
    lateinit var clientInfo: ClientInfo

    override fun getClientInfo() = clientInfo
}
