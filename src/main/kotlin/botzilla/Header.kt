package org.pois_noir.botzilla

internal class Header {
    val status: UByte
    val operationCode: UByte
    val payloadLength: UInt

    constructor(status: UByte,operation: UByte, payloadLength: UInt) {
        this.status = status
        this.operationCode = operation
        this.payloadLength = payloadLength
    }

    fun encode(): ByteArray {
        val result = ByteArray(HEADER_LENGTH)

        result[STATUS_CODE_INDEX] = status.toByte()
        result[OPERATION_CODE_INDEX] = operationCode.toByte()
        result[MESSAGE_LENGTH_INDEX] = ((payloadLength shr 24) and 0xFFu).toByte()
        result[MESSAGE_LENGTH_INDEX + 1] = ((payloadLength shr 16) and 0xFFu).toByte()
        result[MESSAGE_LENGTH_INDEX + 2] = ((payloadLength shr 8) and 0xFFu).toByte()
        result[MESSAGE_LENGTH_INDEX + 3] = (payloadLength and 0xFFu).toByte()

        return result
    }

    companion object {
        fun decode(data: ByteArray): Result<Header> = runCatching {
            require (data.size == HEADER_LENGTH)  { "Header Size not $HEADER_LENGTH" }
            Header(
                data[STATUS_CODE_INDEX].toUByte(),
                data[OPERATION_CODE_INDEX].toUByte(),
                (
                    (data[MESSAGE_LENGTH_INDEX].toInt() and 0xFF) shl 24 or
                    (data[MESSAGE_LENGTH_INDEX + 1].toInt() and 0xFF) shl 16 or
                    (data[MESSAGE_LENGTH_INDEX + 2].toInt() and 0xFF) shl 8 or
                    (data[MESSAGE_LENGTH_INDEX + 3].toInt() and 0xFF)
                ).toUInt()
            )
        }
    }
}

