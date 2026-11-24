package com.example.litutur.data

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object CloudinaryHelper {
    const val UPLOAD_PRESET = "photo_litutur"

    /**
     * Uploads an image Uri directly to Cloudinary using the unsigned preset.
     * Returns the HTTPS secure URL of the uploaded photo.
     */
    suspend fun uploadImage(imageUri: Uri): String = suspendCancellableCoroutine { continuation ->
        val requestId = MediaManager.get().upload(imageUri)
            .unsigned(UPLOAD_PRESET)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as? String
                    if (secureUrl != null) {
                        continuation.resume(secureUrl)
                    } else {
                        continuation.resumeWithException(
                            IllegalStateException("Cloudinary response did not contain secure_url")
                        )
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(
                        RuntimeException("Cloudinary upload failed: ${error.description}")
                    )
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()

        continuation.invokeOnCancellation {
            MediaManager.get().cancelRequest(requestId)
        }
    }
}
