package com.example.litutur.data

import android.content.Context
import android.net.Uri
import com.example.litutur.data.remote.CloudinaryUploader

object CloudinaryHelper {
    suspend fun uploadImage(context: Context, imageUri: Uri, folder: String = "litutur"): String {
        return CloudinaryUploader.uploadImage(context, imageUri, folder).getOrThrow()
    }
}
