package com.example.timelockvault

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeLockManagerTest {
    @Test
    fun generatePassword_hasExpectedLength() {
        val password = TimeLockManager.getInstance(androidx.test.core.app.ApplicationProvider.getApplicationContext())
            .generatePassword(length = 16, includeUppercase = true, includeLowercase = true, includeNumbers = true, includeSymbols = true)
        assertEquals(16, password.length)
    }

    @Test
    fun lockDurationCalculatesRemainingTime() {
        val manager = TimeLockManager.getInstance(androidx.test.core.app.ApplicationProvider.getApplicationContext())
        manager.clearVault()
        manager.lockFor(30_000L)
        val remaining = manager.getRemainingTimeMs()
        assertTrue(remaining in 0..30_000L)
    }
}
