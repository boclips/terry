package com.boclips.terry.infrastructure.incoming

import org.apache.commons.codec.binary.Hex
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

sealed class Result

object SignatureMismatch : Result()
object StaleTimestamp : Result()
object Verified : Result()

class SlackSignature(
    val version: String,
    private val secretKey: ByteArray,
    private val signatureTimeoutSeconds: Int = 5 * 60
) {
    private val type = "HmacSHA256"

    fun verify(request: RawSlackRequest): Result =
        with(request) {
            when {
                timestamp.toLong() < currentTime - signatureTimeoutSeconds ->
                    StaleTimestamp
                compute(timestamp = timestamp, body = body) != signatureClaim ->
                    SignatureMismatch
                else ->
                    Verified
            }
        }

    fun compute(timestamp: String, body: String): String =
        when (secretKey.size) {
            0 ->
                ""
            else ->
                SecretKeySpec(secretKey, type)
                    .let { keySpec ->
                        Mac.getInstance(type)
                            .apply { init(keySpec) }
                            .run { encoded(doFinal(formatted(timestamp, body))) }
                    }
        }

    private fun encoded(text: ByteArray): String =
        "v0=${Hex.encodeHexString(text)}"

    private fun formatted(timestamp: String, body: String) =
        "$version:$timestamp:$body".toByteArray()
}
