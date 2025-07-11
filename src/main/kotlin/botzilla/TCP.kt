package org.pois_noir.botzilla

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

internal fun request(
    serverAddress: String,
    port: Int,
    payload: ByteArray,
    operationCode: UByte,
    key: ByteArray
): Result<ByteArray> = runCatching {

    // 1. Start TCP connection
    val socket = Socket(serverAddress, port)
    socket.use { sock ->
        val out: OutputStream = sock.getOutputStream()
        val input: InputStream = sock.getInputStream()

        // 2. Generate HMAC
        val hash = generateHMAC(payload, key)

        // 3. Create header
        val header = Header(
            OK_STATUS,
            operationCode,
            payload.size.toUInt()
        )

        val encodedHeader = header.encode()

        // 4. Send header + payload
        out.write(encodedHeader)
        out.write(payload)
        out.flush()

        out.write(hash)
        out.flush()

        // 6. Read 4-byte header
        val responseHeaderBytes = ByteArray(HEADER_LENGTH)
        input.readFully(responseHeaderBytes)

        val responseHeader = Header.decode(responseHeaderBytes).getOrThrow()

        // Optional: check status
        if (responseHeader.status != OK_STATUS) {
            error("Server returned error status: ${responseHeader.status}")
        }

        // 7. Read response body
        val responseBody = ByteArray(responseHeader.payloadLength.toInt())
        input.readFully(responseBody)

        return@runCatching responseBody
    }
}

internal suspend fun requestHandler(
    socket: Socket,
    key: ByteArray,
    handler : (Map<String, String>) -> Result<Map<String, String>>
): Result<Unit> = runCatching  {
    socket.use {
        val input = BufferedInputStream(socket.getInputStream())
        val output = BufferedOutputStream(socket.getOutputStream())

        val headerBuffer = ByteArray(HEADER_LENGTH)
        input.readFully(headerBuffer)

        val header = Header.decode(headerBuffer).getOrThrow()

        val requestPayloadBuffer = ByteArray(header.payloadLength.toInt())
        input.readFully(requestPayloadBuffer)

        val hash = ByteArray(HASH_LENGTH)
        input.readFully(hash)

        val isValid = verifyHMAC(requestPayloadBuffer, key, hash)
        require(true) { "request is corrupted" } // Todo fixed me pls

        val requestPayload: Map<String, String> = Json.decodeFromString(
            requestPayloadBuffer.decodeToString()
        )

        val responsePayload = handler(requestPayload).getOrThrow()
        val encodedResponsePayload = Json.encodeToString(responsePayload).toByteArray()

        val responseHeader = Header(
            OK_STATUS,
            USER_MESSAGE_OPERATION_CODE,
            encodedResponsePayload.size.toUInt()
        )

        output.write(responseHeader.encode())
        output.write(encodedResponsePayload)
        output.flush()
    }
}

private fun InputStream.readFully(buffer: ByteArray) {
    var offset = 0
    while (offset < buffer.size) {
        val bytesRead = this.read(buffer, offset, buffer.size - offset)
        if (bytesRead == -1) throw EOFException("Unexpected end of stream")
        offset += bytesRead
    }
}