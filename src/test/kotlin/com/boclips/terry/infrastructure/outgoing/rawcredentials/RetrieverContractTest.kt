package com.boclips.terry.infrastructure.outgoing.rawcredentials

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FakeRawCredentialRetrieverTests : RawCredentialRetrieverTests() {
    @BeforeEach
    fun setUp() {
        retrieverForExistent = FakeRawCredentialRetriever()
            .apply { respondWith(RawCredential(id = "AKIADEFOANAMAZONID1", secret = "password1")) }
        retrieverForMissing = FakeRawCredentialRetriever()
            .apply { respondWith(RawCredentialNotFound) }
    }
}

class IamCredentialRotatorTests : RawCredentialRetrieverTests() {
    @BeforeEach
    fun setUp() {
        retrieverForExistent = IamCredentialRotator()
        retrieverForMissing = IamCredentialRotator()
    }
}

abstract class RawCredentialRetrieverTests {
    var retrieverForExistent: RawCredentialRetriever? = null
    var retrieverForMissing: RawCredentialRetriever? = null

    @Test
    fun `retrieves credential that exists`() {
        val credential = retrieverForExistent!!.get("terry-rotation-tests")
        when (credential) {
            is RawCredential -> {
                assertThat(credential.id).startsWith("AKIA")
                assertThat(credential.secret).isNotBlank()
            }
            RawCredentialNotFound -> fail<String>("Should have got a RawCredential!")
        }
    }

    @Test
    fun `detects that a credential is not found`() {
        assertThat(retrieverForMissing!!.get("missing-channel"))
            .isEqualTo(RawCredentialNotFound)
    }
}
