package com.example.litutur.data.remote

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object CloudinaryUploader {
    private const val CLOUD_NAME = "zmstaqrd"
    private const val UPLOAD_PRESET = "Litutur_Unsigned"
    private const val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Uploads an image Uri to Cloudinary using the unsigned preset.
     * Runs on Dispatchers.IO and returns Result<String> containing the secure_url.
     */
    suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
        folder: String = "litutur"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
            val bytes = contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                ?: return@withContext Result.failure(IOException("Gagal membaca file gambar"))

            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("folder", folder)
                .addFormDataPart(
                    "file",
                    "upload_${System.currentTimeMillis()}.$extension",
                    bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("Upload Cloudinary gagal (${response.code}): $bodyString")
                    )
                }

                val json = JSONObject(bodyString)
                val secureUrl = json.optString("secure_url")
                if (secureUrl.isBlank()) {
                    Result.failure(IOException("secure_url tidak ditemukan pada respons Cloudinary"))
                } else {
                    Result.success(secureUrl)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
