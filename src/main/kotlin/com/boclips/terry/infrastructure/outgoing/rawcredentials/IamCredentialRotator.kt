package com.boclips.terry.infrastructure.outgoing.rawcredentials

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder
import com.amazonaws.services.identitymanagement.model.*

class IamCredentialRotator : RawCredentialRetriever {
    override fun get(channelName: String): RawCredentialResponse {
        val iam = AmazonIdentityManagementClientBuilder
            .standard()
            .withRegion(Regions.EU_WEST_1)
            .withCredentials(EnvironmentVariableCredentialsProvider())
            .build()

        try {
            val accessKey = iam.createAccessKey(CreateAccessKeyRequest(channelName)).accessKey
            return RawCredential(id = accessKey.accessKeyId, secret = accessKey.secretAccessKey)
        } catch (e: LimitExceededException) {
            val listKeysRequest = ListAccessKeysRequest().withUserName(channelName)
            val oldestKey = iam.listAccessKeys(listKeysRequest).accessKeyMetadata
                .sortedBy { it.createDate }
                .first()
                .accessKeyId

            iam.deleteAccessKey(
                DeleteAccessKeyRequest()
                    .withUserName(channelName)
                    .withAccessKeyId(oldestKey)
            )
            return get(channelName)
        } catch (e: NoSuchEntityException) {
            return RawCredentialNotFound
        }
    }

}
