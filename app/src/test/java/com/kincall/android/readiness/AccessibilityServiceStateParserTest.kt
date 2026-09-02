package com.kincall.android.readiness

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityServiceStateParserTest {
    private val full = "com.kincall.android/com.kincall.android.accessibility.WeChatAccessibilityService"
    private val short = "com.kincall.android/.accessibility.WeChatAccessibilityService"

    @Test
    fun findsFullComponentAmongEnabledServices() {
        val enabled = "example/Service:$full:another/Service"

        assertTrue(AccessibilityServiceStateParser.isEnabled(enabled, full, short))
    }

    @Test
    fun acceptsShortFlattenedComponent() {
        assertTrue(AccessibilityServiceStateParser.isEnabled(short, full, short))
    }

    @Test
    fun rejectsNullAndSimilarComponentNames() {
        assertFalse(AccessibilityServiceStateParser.isEnabled(null, full, short))
        assertFalse(
            AccessibilityServiceStateParser.isEnabled("${full}Extra", full, short),
        )
    }
}
