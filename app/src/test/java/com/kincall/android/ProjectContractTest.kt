package com.kincall.android

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectContractTest {
    @Test
    fun applicationIdRemainsStable() {
        assertEquals("com.kincall.android", BuildConfig.APPLICATION_ID)
    }
}
