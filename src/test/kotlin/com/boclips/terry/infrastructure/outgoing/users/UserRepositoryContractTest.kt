package com.boclips.terry.infrastructure.outgoing.users

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class UserRepositoryTest {
    var userRepository: UserRepository? = null

    @Test
    fun `creates a user`() {
        val createResult = userRepository!!.create("my-test-user")
        assertThat(createResult).isInstanceOf(UserCreated::class.java)
        assertThat((createResult as UserCreated).username).isEqualTo("my-test-user")
        assertThat(createResult.userId).startsWith("AIDA")
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