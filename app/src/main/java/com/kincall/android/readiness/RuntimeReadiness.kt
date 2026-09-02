package com.kincall.android.readiness

data class RuntimeReadiness(
    val isWeChatInstalled: Boolean,
    val isAccessibilityServiceEnabled: Boolean,
) {
    val isReady: Boolean
        get() = isWeChatInstalled && isAccessibilityServiceEnabled
}
