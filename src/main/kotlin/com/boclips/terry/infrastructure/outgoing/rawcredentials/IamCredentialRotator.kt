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
            val accessKeyId = accessKey.accessKeyId
            val secretAccessKey = accessKey.secretAccessKey
            return RawCredential(id = accessKeyId, secret = secretAccessKey)
        } catch (e: LimitExceededException) {
            val keys = iam.listAccessKeys(ListAccessKeysRequest().withUserName(channelName))
            val metadata: List<AccessKeyMetadata> = keys.accessKeyMetadata
            iam.deleteAccessKey(
                DeleteAccessKeyRequest()
                    .withUserName(channelName)
                    .withAccessKeyId(
                        metadata.sortedBy { it.createDate }
                            .first()
                            .accessKeyId
                    )
            )
            return get(channelName)
        } catch (e: NoSuchEntityException) {
            return RawCredentialNotFound
        }
    }

}
