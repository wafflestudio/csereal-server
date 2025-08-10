package com.wafflestudio.csereal.common.dto

import java.net.InetAddress
import java.util.*

/**
 * Client identification details captured from an HTTP request.
 *
 * @property ipAddress IP address of the client, resolved to [InetAddress].
 * @property clientId Optional client identifier as a [UUID]. If absent or invalid,
 * it remains `null`.
 */
data class ClientInfo(
    val ipAddress: InetAddress,
    val clientId: UUID? = null
) {
    /**
     * Creates a [ClientInfo] from string representations.
     *
     * - [ipAddress]: IPv4/IPv6 string, resolved via [InetAddress.getByName].
     * - [clientId]: Optional UUID string; invalid values are ignored and treated as `null`.
     */
    constructor(ipAddress: String, clientId: String?) : this(
        ipAddress = ipAddressOf(ipAddress),
        clientId = clientId?.let { clientIdOfOrNull(it) }
    )

    /**
     * Indicates whether this object contains a usable client identifier.
     *
     * @return `true` if [clientId] is not `null`, otherwise `false`.
     */
    fun isValid() = clientId != null
}

private fun ipAddressOf(ipAddress: String) = InetAddress.getByName(ipAddress)
private fun clientIdOfOrNull(clientId: String) = runCatching { UUID.fromString(clientId) }.getOrNull()
