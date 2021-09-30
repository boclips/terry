package com.boclips.terry.infrastructure.outgoing.policy

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class PolicyRepositoryContractTest {
    var policyRepository: PolicyRepository? = null
    var bucketName: String? = null

    @Test
    fun `it creates policy for accessing given storage`() {
        assertThat(policyRepository!!.create("boclips-upload-channel-name")).startsWith("arn::")
    }
}

class AWSPolicyRepositoryTest : PolicyRepositoryContractTest() {

    @BeforeEach
    fun setUp() {
        policyRepository = IamPolicyRepository()
        bucketName = "boclips-upload-channel-name"
    }
}
