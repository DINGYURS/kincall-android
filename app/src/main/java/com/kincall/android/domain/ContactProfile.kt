package com.kincall.android.domain

import java.io.File

data class ContactProfile(
    val displayName: String,
    val photoFile: File?,
)
