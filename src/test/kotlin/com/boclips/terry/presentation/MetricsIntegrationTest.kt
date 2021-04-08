package com.boclips.terry.presentation

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import testsupport.AbstractSpringIntegrationTest

class MetricsIntegrationTest : AbstractSpringIntegrationTest() {
    @Test
    fun `exposes prometheus metrics`() {
        mockMvc.perform(get("/actuator/prometheus"))
            .andExpect(status().isOk)
    }
}
