package com.boclips.terry.infrastructure.outgoing.users

import com.boclips.terry.infrastructure.outgoing.policy.IamPolicyRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class UserRepositoryTest {
    var userRepository: UserRepository? = null
    var policyId: String? = null

    @Test
    fun `creates a user`() {
        val createResult = userRepository!!.create("my-test-user")
        assertThat(createResult).isInstanceOf(UserCreated::class.java)
        assertThat((createResult as UserCreated).username).isEqualTo("my-test-user")
        assertThat(createResult.userId).startsWith("AIDA")
    }

    @Test
    fun `adds a policy to user`() {
        userRepository!!.create("my-test-user")
        policyId = IamPolicyRepository().createOrGet("my-test-policy")
        val addPolicyToUserResult = userRepository!!.addPolicyToUser("my-test-user", policyId!!)

        assertThat(addPolicyToUserResult).isTrue

        val removePolicyFromUserResult =
            userRepository!!.removePolicyFromUser(policyId = policyId!!, userName = "my-test-user")
        assertThat(removePolicyFromUserResult).isTrue
        IamPolicyRepository().delete(policyId = policyId!!)
    }
}

class IamUserRepositoryTest : UserRepositoryTest() {
    @BeforeEach
    internal fun setUp() {
        userRepository = IamUserRepository()
    }
}

class FakeUserRepositoryTest : UserRepositoryTest() {
    @BeforeEach
    internal fun setUp() {
        userRepository = FakeUserRepository()
    }
}
