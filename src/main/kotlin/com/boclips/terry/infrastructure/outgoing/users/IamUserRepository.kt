package com.boclips.terry.infrastructure.outgoing.users

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder
import com.amazonaws.services.identitymanagement.model.*

class IamUserRepository : UserRepository {
    private val iam = AmazonIdentityManagementClientBuilder
        .standard()
        .withRegion(Regions.EU_WEST_1)
        .withCredentials(EnvironmentVariableCredentialsProvider())
        .build()

    override fun create(user: String): UserCreationResponse {
        return try {
            val createdUser = iam.createUser(CreateUserRequest().withUserName(user))
            UserCreated(username = createdUser.user.userName, userId = createdUser.user.userId)
        } catch (ex: EntityAlreadyExistsException) {
            val existingUser = iam.getUser(GetUserRequest().withUserName(user))
            UserCreated(username = existingUser.user.userName, userId = existingUser.user.userId)
        }
    }

    override fun addPolicyToUser(userName: String, policyId: String): Boolean {
        val attachUserPolicy = iam.attachUserPolicy(
            AttachUserPolicyRequest()
                .withUserName(userName)
                .withPolicyArn(policyId)
        )

        return attachUserPolicy.sdkHttpMetadata.httpStatusCode == 200
    }

    override fun removePolicyFromUser(userName: String, policyId: String): Boolean =
        iam.detachUserPolicy(
            DetachUserPolicyRequest().withPolicyArn(policyId).withUserName(userName)
        ).sdkHttpMetadata.httpStatusCode == 200
}
