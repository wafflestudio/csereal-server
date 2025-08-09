package com.wafflestudio.csereal.common.context

import com.wafflestudio.csereal.common.dto.ClientInfo
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@Component
@RequestScope
class ClientInfoHolder {
    lateinit var clientInfo: ClientInfo
        internal set
}
