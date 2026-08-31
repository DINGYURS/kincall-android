package com.kincall.android.domain

class CallRequestGate {
    private var requestInFlight = false

    @Synchronized
    fun tryAcquire(): Boolean {
        if (requestInFlight) return false
        requestInFlight = true
        return true
    }

    @Synchronized
    fun release() {
        requestInFlight = false
    }
}
