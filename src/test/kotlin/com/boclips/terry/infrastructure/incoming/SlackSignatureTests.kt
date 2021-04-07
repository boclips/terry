package com.boclips.terry.infrastructure.incoming

import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.properties.Gen
import io.kotlintest.properties.assertAll
import org.junit.jupiter.api.Test

class SlackSignatureTests {
    @Test
    fun `known-good signature succeeds`() {
        val signer = SlackSignature(
            version = "123",
            secretKey = "mysecret".toByteArray()
        )
        val sigTime: Long = 1234567
        val body = "foo"
        val goodSig = "v0=d05129261e5a96d73293416180167bdd18dea6d7f3e598ec5bacb75d3db24b75"

        signer.verify(
            RawSlackRequest(
                currentTime = sigTime,
                body = body,
                timestamp = "$sigTime",
                signatureClaim = goodSig
            )
        ).shouldBeSameInstanceAs(Verified)
    }

    @Test
    fun `own signature succeeds`() {
        assertAll(
            Gen.string(), Gen.string(), Gen.nats(), Gen.string()
        ) { version: String, secretKey: String, sigTime: Int, body: String ->
            with(SlackSignature(version, secretKey.toByteArray())) {
                verify(
                    RawSlackRequest(
                        currentTime = sigTime.toLong(),
                        body = body,
                        timestamp = "$sigTime",
                        signatureClaim = compute(timestamp = "$sigTime", body = body)
                    )
                ).shouldBeSameInstanceAs(Verified)
            }
        }
    }

    @Test
    fun `late verification of own signature fails`() {
        assertAll(
            Gen.string(), Gen.string(), Gen.nats(), Gen.string(), Gen.nats()
        ) { version: String, secretKey: String, sigTime: Int, body: String, timeoutSeconds: Int ->
            with(SlackSignature(version, secretKey.toByteArray(), signatureTimeoutSeconds = timeoutSeconds)) {
                verify(
                    RawSlackRequest(
                        currentTime = sigTime.toLong() + timeoutSeconds + 1,
                        body = body,
                        timestamp = "$sigTime",
                        signatureClaim = compute(timestamp = "$sigTime", body = body)
                    )
                ).shouldBeSameInstanceAs(StaleTimestamp)
            }
        }
    }
}
