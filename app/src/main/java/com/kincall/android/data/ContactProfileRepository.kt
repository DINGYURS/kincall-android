package com.kincall.android.data

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import com.kincall.android.domain.ContactProfile
import com.kincall.android.domain.ProfileValidator
import com.kincall.android.domain.ValidationResult
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class ContactProfileRepository(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val profileDirectory = File(appContext.filesDir, PROFILE_DIRECTORY)
    private val profilePhoto = File(profileDirectory, PROFILE_PHOTO_FILE)

    fun load(): ContactProfile? {
        val displayName = preferences.getString(KEY_DISPLAY_NAME, null) ?: return null
        val photo = profilePhoto.takeIf(File::isFile)
        return ContactProfile(displayName = displayName, photoFile = photo)
    }

    @Throws(IOException::class, IllegalArgumentException::class)
    fun save(displayName: String, newPhoto: InputStream?): ContactProfile {
        val normalizedName = when (val result = ProfileValidator.validateDisplayName(displayName)) {
            is ValidationResult.Valid -> result.normalizedDisplayName
            is ValidationResult.Invalid -> throw IllegalArgumentException(result.error.name)
        }

        if (newPhoto != null) {
            importPhoto(newPhoto)
        }

        if (!preferences.edit().putString(KEY_DISPLAY_NAME, normalizedName).commit()) {
            throw IOException("Unable to persist contact profile")
        }

        return ContactProfile(
            displayName = normalizedName,
            photoFile = profilePhoto.takeIf(File::isFile),
        )
    }

    @SuppressLint("ApplySharedPref")
    internal fun clear() {
        preferences.edit().clear().commit()
        profilePhoto.delete()
        File(profileDirectory, TEMP_PROFILE_PHOTO_FILE).delete()
    }

    @Throws(IOException::class)
    private fun importPhoto(input: InputStream) {
        if (!profileDirectory.exists() && !profileDirectory.mkdirs()) {
            throw IOException("Unable to create profile directory")
        }

        val temporaryPhoto = File(profileDirectory, TEMP_PROFILE_PHOTO_FILE)
        try {
            temporaryPhoto.outputStream().buffered().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var totalBytes = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    totalBytes += read
                    if (totalBytes > MAX_PHOTO_BYTES) {
                        throw IOException("Selected photo exceeds the size limit")
                    }
                    output.write(buffer, 0, read)
                }
                output.flush()
            }

            if (temporaryPhoto.length() == 0L) {
                throw IOException("Selected photo is empty")
            }

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(temporaryPhoto.absolutePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                throw IOException("Selected file is not a supported image")
            }

            try {
                Files.move(
                    temporaryPhoto.toPath(),
                    profilePhoto.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING,
                )
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(
                    temporaryPhoto.toPath(),
                    profilePhoto.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                )
            }
        } finally {
            temporaryPhoto.delete()
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "contact_profile"
        const val KEY_DISPLAY_NAME = "display_name"
        const val PROFILE_DIRECTORY = "profile"
        const val PROFILE_PHOTO_FILE = "contact_photo"
        const val TEMP_PROFILE_PHOTO_FILE = "contact_photo.tmp"
        const val MAX_PHOTO_BYTES = 10L * 1024L * 1024L
    }
}
