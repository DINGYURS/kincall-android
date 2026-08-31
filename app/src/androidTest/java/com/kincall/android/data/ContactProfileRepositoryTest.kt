package com.kincall.android.data

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContactProfileRepositoryTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val repository = ContactProfileRepository(context)

    @Before
    fun setUp() = repository.clear()

    @After
    fun tearDown() = repository.clear()

    @Test
    fun savesAndReloadsNameAndPrivatePhoto() {
        repository.save("  家人  ", ByteArrayInputStream(validPng()))
        val reloaded = repository.load()

        assertNotNull(reloaded)
        assertEquals("家人", reloaded?.displayName)
        assertTrue(reloaded?.photoFile?.isFile == true)
        assertTrue(reloaded?.photoFile?.canonicalPath?.startsWith(context.filesDir.canonicalPath) == true)
    }

    @Test
    fun preservesPhotoWhenOnlyDisplayNameChanges() {
        repository.save("家人", ByteArrayInputStream(validPng()))

        repository.save("孙子", null)
        val reloaded = repository.load()

        assertEquals("孙子", reloaded?.displayName)
        assertTrue(reloaded?.photoFile?.isFile == true)
    }

    @Test(expected = IOException::class)
    fun rejectsContentThatIsNotAnImage() {
        repository.save("家人", ByteArrayInputStream("not an image".toByteArray()))
    }

    private fun validPng(): ByteArray = ByteArrayOutputStream().use { output ->
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
        bitmap.recycle()
        output.toByteArray()
    }
}
