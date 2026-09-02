package com.kincall.android.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class WeChatAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // F03 intentionally performs no actions. F04 will route validated WeChat events to a state machine.
    }

    override fun onInterrupt() = Unit
}
