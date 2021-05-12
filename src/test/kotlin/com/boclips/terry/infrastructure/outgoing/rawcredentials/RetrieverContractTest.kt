package com.boclips.terry.infrastructure.outgoing.rawcredentials

import com.google.cloud.storage.StorageOptions
import org.assertj.core.api.Assertions.assertThat
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

class CloudStorageRetrieverTests : RawCredentialRetrieverTests() {
    @BeforeEach
    fun setUp() {
        retrieverForExistent = CloudStorageRetriever(
            storage = StorageOptions.getDefaultInstance().service,
            bucketName = "boclips-terraform-channels-test"
        )
        retrieverForMissing = CloudStorageRetriever(
            storage = StorageOptions.getDefaultInstance().service,
            bucketName = "boclips-terraform-channels-test"
        )
    }
}

abstract class RawCredentialRetrieverTests {
    var retrieverForExistent: RawCredentialRetriever? = null
    var retrieverForMissing: RawCredentialRetriever? = null

    @Test
    fun `retrieves credential that exists`() {
        assertThat(retrieverForExistent!!.get("3blue1brown"))
            .isEqualTo(RawCredential(id = "AKIADEFOANAMAZONID1", secret = "password1"))
    }

    @Test
    fun `detects that a credential is not found`() {
        assertThat(retrieverForMissing!!.get("missing-channel"))
            .isEqualTo(RawCredentialNotFound)
    }
}
