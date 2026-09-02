package com.kincall.android.accessibility

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kincall.android.readiness.RuntimeReadinessChecker
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParser

@RunWith(AndroidJUnit4::class)
class AccessibilityServiceContractTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun serviceRequiresSystemBindingPermission() {
        val service = context.packageManager.getServiceInfo(
            ComponentName(context, WeChatAccessibilityService::class.java),
            0,
        )

        assertEquals(Manifest.permission.BIND_ACCESSIBILITY_SERVICE, service.permission)
        assertTrue(service.exported)
    }

    @Test
    fun serviceIsRestrictedToWeChatAndRequiredCapabilities() {
        val service = context.packageManager.getServiceInfo(
            ComponentName(context, WeChatAccessibilityService::class.java),
            PackageManager.GET_META_DATA,
        )
        val configId = service.metaData.getInt(AccessibilityService.SERVICE_META_DATA)
        val parser = context.packageManager
            .getResourcesForApplication(service.applicationInfo)
            .getXml(configId)

        parser.use {
            while (parser.eventType != XmlPullParser.START_TAG) {
                parser.next()
            }
            assertEquals("accessibility-service", parser.name)
            assertArrayEquals(
                arrayOf(RuntimeReadinessChecker.WECHAT_PACKAGE_NAME),
                parser.getAttributeValue(ANDROID_NAMESPACE, "packageNames").split(',').toTypedArray(),
            )
            assertTrue(parser.getAttributeBooleanValue(ANDROID_NAMESPACE, "canRetrieveWindowContent", false))
            assertTrue(parser.getAttributeBooleanValue(ANDROID_NAMESPACE, "canPerformGestures", false))
        }
    }

    private companion object {
        const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
    }
}
