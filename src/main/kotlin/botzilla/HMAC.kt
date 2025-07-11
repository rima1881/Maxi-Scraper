package org.pois_noir.botzilla

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest

internal fun generateHMAC(data: ByteArray, key: ByteArray): ByteArray {
    val mac = Mac.getInstance("HmacSHA256") // 32-byte output
    val secretKey = SecretKeySpec(key, "HmacSHA256")
    mac.init(secretKey)
    return mac.doFinal(data)
}

internal fun verifyHMAC(data: ByteArray, key: ByteArray, hash: ByteArray): Boolean {
    val generatedHMAC = generateHMAC(data, key)
    return MessageDigest.isEqual(generatedHMAC, hash)
}

