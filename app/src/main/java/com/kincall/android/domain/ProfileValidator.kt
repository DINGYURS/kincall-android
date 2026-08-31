package com.kincall.android.domain

object ProfileValidator {
    const val MAX_DISPLAY_NAME_CODE_POINTS = 24

    fun normalizeDisplayName(input: String): String = input.trim()

    fun validateDisplayName(input: String): ValidationResult {
        val normalized = normalizeDisplayName(input)
        if (normalized.isEmpty()) {
            return ValidationResult.Invalid(ValidationError.EMPTY_DISPLAY_NAME)
        }

        val codePoints = normalized.codePointCount(0, normalized.length)
        if (codePoints > MAX_DISPLAY_NAME_CODE_POINTS) {
            return ValidationResult.Invalid(ValidationError.DISPLAY_NAME_TOO_LONG)
        }

        return ValidationResult.Valid(normalized)
    }
}

sealed interface ValidationResult {
    data class Valid(val normalizedDisplayName: String) : ValidationResult

    data class Invalid(val error: ValidationError) : ValidationResult
}

enum class ValidationError {
    EMPTY_DISPLAY_NAME,
    DISPLAY_NAME_TOO_LONG,
}
