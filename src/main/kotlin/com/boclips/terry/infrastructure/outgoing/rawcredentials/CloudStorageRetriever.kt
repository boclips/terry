package com.boclips.terry.infrastructure.outgoing.rawcredentials

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage

class CloudStorageRetriever(
    private val storage: Storage,
    private val bucketName: String
) : RawCredentialRetriever {
    override fun get(channelName: String): RawCredentialResponse =
        terraformState()
            .resources
            .filterIsInstance<AwsIamAccessKey>()
            .find { accessKey -> accessKey.instances.first().attributes.user == channelName }
            ?.instances?.first()?.attributes
            ?.run { RawCredential(id, secret) }
            ?: RawCredentialNotFound

    private fun terraformState(): TerraformState =
        jacksonObjectMapper().readValue(json(), TerraformState::class.java)

    private fun json(): String =
        String(storage.get(BlobId.of(bucketName, "default.tfstate")).getContent())
}
