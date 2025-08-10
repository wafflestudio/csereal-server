package com.wafflestudio.csereal.common.dto

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
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
    constructor(ipAddress: String, clientId: String? = null) : this(
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
private fun clientIdOfOrNull(clientId: String) =
    runCatching { UUID.fromString(clientId) }.getOrNull()

@Converter(autoApply = true)
class ClientInfoConverter : AttributeConverter<ClientInfo, String> {
    /**
     * Converts the value stored in the entity attribute into the
     * data representation to be stored in the database.
     *
     * @param attribute  the entity attribute value to be converted
     * @return  the converted data to be stored in the database
     * column
     */
    override fun convertToDatabaseColumn(attribute: ClientInfo?): String? =
        attribute?.let {
            "${it.ipAddress.hostAddress}/${it.clientId?.toString() ?: ""}"
        }

    /**
     * Converts the data stored in the database column into the
     * value to be stored in the entity attribute.
     * Note that it is the responsibility of the converter writer to
     * specify the correct `dbData` type for the corresponding
     * column for use by the JDBC driver: i.e., persistence providers are
     * not expected to do such type conversion.
     *
     * @param dbData  the data from the database column to be
     * converted
     * @return  the converted value to be stored in the entity
     * attribute
     */
    override fun convertToEntityAttribute(dbData: String?): ClientInfo? =
        dbData?.let {
            val divided = it.split("/", limit = 1)

            runCatching {
                when (divided.size) {
                    1 -> ClientInfo(ipAddress = divided[0])
                    2 -> ClientInfo(
                        ipAddress = divided[0],
                        clientId = divided[1],
                    )
                    else -> throw IllegalArgumentException("Invalid format for ClientInfo")
                }
            }.getOrNull()
        }
}
