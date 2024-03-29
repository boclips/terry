package com.boclips.terry.infrastructure.outgoing.policy

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder
import com.amazonaws.services.identitymanagement.model.CreatePolicyRequest
import com.amazonaws.services.identitymanagement.model.DeletePolicyRequest

class IamPolicyRepository : PolicyRepository {
    val iam = AmazonIdentityManagementClientBuilder
        .standard()
        .withRegion(Regions.EU_WEST_1)
        .withCredentials(EnvironmentVariableCredentialsProvider())
        .build()

    override fun createOrGet(storageName: String): String? = try {
        iam.createPolicy(
            CreatePolicyRequest()
                .withPolicyName(storageName)
                .withPolicyDocument(policyGenerator(storageName))
        ).policy.arn
    } catch (ex: Exception) {
        iam.listPolicies().policies.find { it.policyName == storageName }?.arn
    }

    override fun delete(policyId: String): Boolean {
        return iam.deletePolicy(DeletePolicyRequest().withPolicyArn(policyId))?.let {
            it.sdkHttpMetadata.httpStatusCode == 200
        } ?: false
    }

    private fun policyGenerator(bucketName: String): String =
        """{
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Resource": [
                                "arn:aws:s3:::$bucketName/*"
                            ],
                            "Action": [
                                "s3:PutObject"
                            ]
                        },
                        {
                            "Effect": "Allow",
                            "Resource": [
                                "arn:aws:s3:::$bucketName"
                            ],
                            "Action": [
                                "s3:ListBucket",
                                "s3:GetBucketLocation"
                            ]
                        },
                        {
                            "Effect": "Allow",
                            "Resource": [
                                "arn:aws:s3:::*"
                            ],
                            "Action": [
                                "s3:ListAllMyBuckets"
                            ]
                        }
                    ]
            }
        """.trimMargin()
}
