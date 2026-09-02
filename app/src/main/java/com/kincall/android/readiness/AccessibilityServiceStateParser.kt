package com.kincall.android.readiness

object AccessibilityServiceStateParser {
    fun isEnabled(
        enabledServices: String?,
        fullComponentName: String,
        shortComponentName: String,
    ): Boolean = enabledServices
        ?.split(COMPONENT_SEPARATOR)
        ?.any { component ->
            component.equals(fullComponentName, ignoreCase = true) ||
                component.equals(shortComponentName, ignoreCase = true)
        } == true

    private const val COMPONENT_SEPARATOR = ':'
}
