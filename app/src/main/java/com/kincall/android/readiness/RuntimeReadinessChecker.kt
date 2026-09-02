package com.kincall.android.readiness

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import com.kincall.android.accessibility.WeChatAccessibilityService

class RuntimeReadinessChecker(context: Context) {
    private val appContext = context.applicationContext

    fun check(): RuntimeReadiness = RuntimeReadiness(
        isWeChatInstalled = isPackageInstalled(WECHAT_PACKAGE_NAME),
        isAccessibilityServiceEnabled = isAccessibilityServiceEnabled(),
    )

    private fun isPackageInstalled(packageName: String): Boolean = try {
        appContext.packageManager.getApplicationInfo(packageName, 0).enabled
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val globallyEnabled = Settings.Secure.getInt(
            appContext.contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED,
            0,
        ) == 1
        if (!globallyEnabled) return false

        val enabledServices = Settings.Secure.getString(
            appContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        )
        val component = ComponentName(appContext, WeChatAccessibilityService::class.java)
        return AccessibilityServiceStateParser.isEnabled(
            enabledServices = enabledServices,
            fullComponentName = component.flattenToString(),
            shortComponentName = component.flattenToShortString(),
        )
    }

    companion object {
        const val WECHAT_PACKAGE_NAME = "com.tencent.mm"
    }
}
