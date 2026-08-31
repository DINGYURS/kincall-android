package com.kincall.android

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.kincall.android.data.ContactProfileRepository
import com.kincall.android.domain.CallRequestGate
import com.kincall.android.domain.ContactProfile
import com.kincall.android.domain.ProfileValidator
import com.kincall.android.domain.ValidationError
import com.kincall.android.domain.ValidationResult
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val repository by lazy { ContactProfileRepository(this) }
    private val callRequestGate = CallRequestGate()
    private val ioExecutor = Executors.newSingleThreadExecutor()

    private lateinit var setupContainer: View
    private lateinit var homeContainer: View
    private lateinit var nameInput: EditText
    private lateinit var setupPhoto: ImageView
    private lateinit var callPhoto: ImageButton
    private lateinit var contactName: TextView
    private lateinit var callStatus: TextView
    private lateinit var saveButton: Button

    private var pendingPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pendingPhotoUri = savedInstanceState?.getString(STATE_PENDING_PHOTO_URI)?.let(Uri::parse)

        bindViews()
        bindActions()

        val profile = repository.load()
        if (profile == null) {
            showSetup(null)
        } else {
            showHome(profile)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        pendingPhotoUri?.let { outState.putString(STATE_PENDING_PHOTO_URI, it.toString()) }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        ioExecutor.shutdownNow()
        super.onDestroy()
    }

    @Deprecated("Uses the platform document picker for API 26 compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_PICK_PHOTO || resultCode != RESULT_OK) return

        val uri = data?.data ?: return
        pendingPhotoUri = uri
        setupPhoto.setImageURI(uri)
        setupPhoto.contentDescription = getString(R.string.selected_contact_photo)
    }

    private fun bindViews() {
        setupContainer = findViewById(R.id.setup_container)
        homeContainer = findViewById(R.id.home_container)
        nameInput = findViewById(R.id.contact_name_input)
        setupPhoto = findViewById(R.id.setup_photo)
        callPhoto = findViewById(R.id.call_photo)
        contactName = findViewById(R.id.contact_name)
        callStatus = findViewById(R.id.call_status)
        saveButton = findViewById(R.id.save_profile_button)
        setupPhoto.clipToOutline = true
        callPhoto.clipToOutline = true
    }

    private fun bindActions() {
        findViewById<Button>(R.id.choose_photo_button).setOnClickListener { choosePhoto() }
        findViewById<Button>(R.id.edit_profile_button).setOnClickListener {
            showSetup(repository.load())
        }
        findViewById<Button>(R.id.cancel_setup_button).setOnClickListener {
            repository.load()?.let(::showHome)
        }
        saveButton.setOnClickListener { saveProfile() }
        callPhoto.setOnClickListener { handleCallRequest() }
    }

    @Suppress("DEPRECATION")
    private fun choosePhoto() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_PICK_PHOTO)
    }

    private fun saveProfile() {
        nameInput.error = null
        when (val validation = ProfileValidator.validateDisplayName(nameInput.text.toString())) {
            is ValidationResult.Invalid -> {
                nameInput.error = when (validation.error) {
                    ValidationError.EMPTY_DISPLAY_NAME -> getString(R.string.error_name_required)
                    ValidationError.DISPLAY_NAME_TOO_LONG -> getString(
                        R.string.error_name_too_long,
                        ProfileValidator.MAX_DISPLAY_NAME_CODE_POINTS,
                    )
                }
                nameInput.requestFocus()
            }

            is ValidationResult.Valid -> persistProfile(validation.normalizedDisplayName)
        }
    }

    private fun persistProfile(displayName: String) {
        saveButton.isEnabled = false
        val photoUri = pendingPhotoUri
        ioExecutor.execute {
            val outcome = try {
                val profile = photoUri?.let { uri ->
                    contentResolver.openInputStream(uri)?.use { input ->
                        repository.save(displayName, input)
                    } ?: throw IOException("Unable to open selected photo")
                } ?: repository.save(displayName, null)
                SaveOutcome.Success(profile)
            } catch (_: IOException) {
                SaveOutcome.Failure
            }

            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                saveButton.isEnabled = true
                when (outcome) {
                    is SaveOutcome.Success -> {
                        pendingPhotoUri = null
                        showHome(outcome.profile)
                    }

                    SaveOutcome.Failure -> {
                        nameInput.error = getString(R.string.error_profile_save)
                    }
                }
            }
        }
    }

    private fun showSetup(profile: ContactProfile?) {
        homeContainer.visibility = View.GONE
        setupContainer.visibility = View.VISIBLE
        nameInput.setText(profile?.displayName.orEmpty())
        findViewById<Button>(R.id.cancel_setup_button).visibility =
            if (profile == null) View.GONE else View.VISIBLE

        when {
            pendingPhotoUri != null -> setupPhoto.setImageURI(pendingPhotoUri)
            profile?.photoFile != null -> setPrivatePhoto(setupPhoto, profile.photoFile)
            else -> setupPhoto.setImageResource(R.drawable.ic_person)
        }
    }

    private fun showHome(profile: ContactProfile) {
        setupContainer.visibility = View.GONE
        homeContainer.visibility = View.VISIBLE
        contactName.text = profile.displayName
        callPhoto.contentDescription = getString(R.string.call_contact_description, profile.displayName)
        callStatus.text = getString(R.string.tap_photo_instruction)
        callPhoto.isEnabled = true

        if (profile.photoFile != null) {
            setPrivatePhoto(callPhoto, profile.photoFile)
        } else {
            callPhoto.setImageResource(R.drawable.ic_person)
        }
    }

    private fun handleCallRequest() {
        if (!callRequestGate.tryAcquire()) return

        callPhoto.isEnabled = false
        callStatus.text = getString(R.string.call_service_not_ready)
        callStatus.announceForAccessibility(callStatus.text)
        callStatus.postDelayed(
            {
                callRequestGate.release()
                callPhoto.isEnabled = true
            },
            CALL_FEEDBACK_MILLIS,
        )
    }

    private fun setPrivatePhoto(view: ImageView, file: File) {
        decodeScaledBitmap(file, MAX_IMAGE_DIMENSION)?.let(view::setImageBitmap)
            ?: view.setImageResource(R.drawable.ic_person)
    }

    private fun decodeScaledBitmap(file: File, maxDimension: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)

        var sampleSize = 1
        while (bounds.outWidth / sampleSize > maxDimension ||
            bounds.outHeight / sampleSize > maxDimension
        ) {
            sampleSize *= 2
        }

        return BitmapFactory.decodeFile(
            file.absolutePath,
            BitmapFactory.Options().apply { inSampleSize = sampleSize },
        )
    }

    private companion object {
        const val REQUEST_PICK_PHOTO = 1001
        const val STATE_PENDING_PHOTO_URI = "pending_photo_uri"
        const val MAX_IMAGE_DIMENSION = 1024
        const val CALL_FEEDBACK_MILLIS = 1_500L
    }
}

private sealed interface SaveOutcome {
    data class Success(val profile: ContactProfile) : SaveOutcome

    data object Failure : SaveOutcome
}
