package com.kincall.android

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            TextView(this).apply {
                gravity = Gravity.CENTER
                text = getString(R.string.foundation_ready)
                textSize = 24f
            },
        )
    }
}
