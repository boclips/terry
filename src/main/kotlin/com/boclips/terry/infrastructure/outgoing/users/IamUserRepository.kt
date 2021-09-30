package com.boclips.terry.infrastructure.outgoing.users

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder
import com.amazonaws.services.identitymanagement.model.AttachUserPolicyRequest
import com.amazonaws.services.identitymanagement.model.CreateUserRequest
import com.amazonaws.services.identitymanagement.model.EntityAlreadyExistsException
import com.amazonaws.services.identitymanagement.model.GetUserRequest

class IamUserRepository : UserRepository {
    override fun create(user: String): UserCreationResponse {
        val iam = AmazonIdentityManagementClientBuilder
            .standard()
            .withRegion(Regions.EU_WEST_1)
            .withCredentials(EnvironmentVariableCredentialsProvider())
            .build()
        return try {
            val createdUser = iam.createUser(CreateUserRequest().withUserName(user))
            UserCreated(username = createdUser.user.userName, userId = createdUser.user.userId)
        } catch (ex: EntityAlreadyExistsException) {
            val existingUser = iam.getUser(GetUserRequest().withUserName(user))
            UserCreated(username = existingUser.user.userName, userId = existingUser.user.userId)
        }
    }
}
