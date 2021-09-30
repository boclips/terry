package com.boclips.terry.infrastructure.outgoing.policy

import io.kotlintest.specs.AbstractAnnotationSpec
import org.aspectj.lang.annotation.Before
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class PolicyRepositoryContractTest {
    var policyRepository: PolicyRepository? = null
    var bucketName: String? = null
    var existingPolicyName: String? = null
    var newPolicyArn: String? = null


    @Test
    fun `it creates a policy for accessing given storage and returns an address`() {
        newPolicyArn = policyRepository!!.create("boclips-upload-test")
        assertThat(newPolicyArn).startsWith("arn:")
    }

    @Test
    fun `it can delete a given policy`() {
        assertThat(policyRepository!!.delete("boclips-upload-channel-name1")).isTrue
    }
}

class AWSPolicyRepositoryTest : PolicyRepositoryContractTest() {

    @BeforeEach
    fun setUp() {
        policyRepository = IamPolicyRepository()
        (policyRepository as IamPolicyRepository).delete("boclips-upload-test")
        policyRepository?.create("boclips-upload-channel-name1")
    }

}

class FakePolicyRepositoryTest : PolicyRepositoryContractTest() {

    @BeforeEach
    fun setUp() {
        policyRepository = FakePolicyRepository()
        (policyRepository as FakePolicyRepository).delete("boclips-upload-test")
        policyRepository?.create("boclips-upload-channel-name1")

    }
}
