package com.boclips.terry.infrastructure.outgoing.rawcredentials

import com.google.cloud.storage.StorageOptions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FakeRetrieverTests : RetrieverTests() {
    @BeforeEach
    fun setUp() {
        retrieverForExistent = FakeRetriever()
            .apply { respondWith(Credential(id = "AKIADEFOANAMAZONID1", secret = "password1")) }
        retrieverForMissing = FakeRetriever()
            .apply { respondWith(CredentialNotFound) }
    }
}

class CloudStorageRetrieverTests : RetrieverTests() {
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

abstract class RetrieverTests {
    var retrieverForExistent: Retriever? = null
    var retrieverForMissing: Retriever? = null

    @Test
    fun `retrieves credential that exists`() {
        assertThat(retrieverForExistent!!.get("3blue1brown"))
            .isEqualTo(Credential(id = "AKIADEFOANAMAZONID1", secret = "password1"))
    }

    @Test
    fun `detects that a credential is not found`() {
        assertThat(retrieverForMissing!!.get("missing-channel"))
            .isEqualTo(CredentialNotFound)
    }
}
