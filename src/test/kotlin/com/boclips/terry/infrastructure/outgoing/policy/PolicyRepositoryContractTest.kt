package com.boclips.terry.infrastructure.outgoing.policy

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class PolicyRepositoryContractTest {
    var policyRepository: PolicyRepository? = null
    var bucketName: String? = null
    var newPolicyArn: String? = null


    @Test
    fun `can create and delete a policy for accessing given storage with arn`() {
        newPolicyArn = policyRepository!!.create("boclips-upload-test")
        assertThat(newPolicyArn).startsWith("arn:")

        assertThat(policyRepository!!.delete(newPolicyArn!!)).isTrue
    }

}

class AWSPolicyRepositoryTest : PolicyRepositoryContractTest() {

    @BeforeEach
    fun setUp() {
        policyRepository = IamPolicyRepository()
    }

}

class FakePolicyRepositoryTest : PolicyRepositoryContractTest() {

    @BeforeEach
    fun setUp() {
        policyRepository = FakePolicyRepository()
    }
}
