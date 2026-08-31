package com.kincall.android.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileValidatorTest {
    @Test
    fun trimsAndAcceptsAValidName() {
        val result = ProfileValidator.validateDisplayName("  晨埕  ")

        assertEquals(ValidationResult.Valid("晨埕"), result)
    }

    @Test
    fun rejectsBlankName() {
        val result = ProfileValidator.validateDisplayName("  \n ")

        assertEquals(
            ValidationResult.Invalid(ValidationError.EMPTY_DISPLAY_NAME),
            result,
        )
    }

    @Test
    fun countsEmojiAsOneCodePoint() {
        val name = "🙂".repeat(ProfileValidator.MAX_DISPLAY_NAME_CODE_POINTS)

        assertTrue(ProfileValidator.validateDisplayName(name) is ValidationResult.Valid)
    }

    @Test
    fun rejectsNamePastCodePointLimit() {
        val name = "家".repeat(ProfileValidator.MAX_DISPLAY_NAME_CODE_POINTS + 1)

        assertEquals(
            ValidationResult.Invalid(ValidationError.DISPLAY_NAME_TOO_LONG),
            ProfileValidator.validateDisplayName(name),
        )
    }
}
