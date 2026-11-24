package com.example.litutur

import android.app.Application
import com.cloudinary.android.MediaManager

class LituturApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inisialisasi Cloudinary SDK
        val config = hashMapOf(
            "cloud_name" to "hyefkzje",
            "secure" to true
        )
        try {
            MediaManager.init(this, config)
        } catch (_: Exception) {
            // Sudah diinisialisasi
        }
    }
}
