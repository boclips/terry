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
        newPolicyArn = policyRepository!!.createOrGet("boclips-upload-test")
        assertThat(newPolicyArn).startsWith("arn:")

        assertThat(policyRepository!!.delete(newPolicyArn!!)).isTrue
    }

    @Test
    fun `gets a policy if it exists already`() {
        assertThat(policyRepository!!.createOrGet("my-test-policy1")).startsWith("arn:")
    }
}

class AWSPolicyRepositoryTest : PolicyRepositoryContractTest() {

    @BeforeEach
    fun setUp() {
        policyRepository = IamPolicyRepository()
        policyRepository!!.createOrGet("my-test-policy1")
    }
}

class FakePolicyRepositoryTest : PolicyRepositoryContractTest() {

    @BeforeEach
    fun setUp() {
        policyRepository = FakePolicyRepository()
        policyRepository!!.createOrGet("my-test-policy1")
    }
}
