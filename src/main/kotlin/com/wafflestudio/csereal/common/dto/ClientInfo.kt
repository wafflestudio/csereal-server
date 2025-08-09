package com.wafflestudio.csereal.common.dto

import java.net.InetAddress
import java.util.*

data class ClientInfo(
    val ipAddress: InetAddress, val clientId: UUID? = null
) {
    constructor(ipAddress: String, clientId: String?) : this(
        ipAddress = ipAddressOf(ipAddress),
        clientId = clientId?.let { clientIdOfOrNull(it) },
    )

    fun isValid() = clientId != null
}

private fun ipAddressOf(ipAddress: String) = InetAddress.getByName(ipAddress)
private fun clientIdOfOrNull(clientId: String) = runCatching { UUID.fromString(clientId) }.getOrNull()
