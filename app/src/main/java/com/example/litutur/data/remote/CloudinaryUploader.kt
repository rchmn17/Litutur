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

/**
 * Upload gambar ke Cloudinary memakai Unsigned Upload Preset.
 * Hanya butuh cloud name + nama preset (API Secret TIDAK boleh ada di aplikasi).
 */
object CloudinaryUploader {
    const val CLOUD_NAME = "zmstaqrd"
    private const val UPLOAD_PRESET = "Litutur_Unsigned"
    private const val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
    const val DELIVERY_HOST = "res.cloudinary.com"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    /** True jika URL sudah berada di Cloudinary milik Litutur */
    fun isCloudinaryUrl(url: String?): Boolean =
        !url.isNullOrBlank() && url.contains("$DELIVERY_HOST/$CLOUD_NAME/")

    /** URL pengiriman (delivery) Cloudinary untuk public_id tertentu */
    fun deliveryUrl(folder: String, publicId: String): String =
        "https://$DELIVERY_HOST/$CLOUD_NAME/image/upload/$folder/$publicId"

    /**
     * Upload foto dari galeri / kamera (content Uri). Dipakai foto profil anak & postingan komunitas.
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
            uploadBytesInternal(bytes, mimeType, folder, publicId = null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload gambar dari link publik: Cloudinary sendiri yang mengunduh gambarnya (remote fetch).
     * Dipakai untuk memindahkan ilustrasi & stiker bawaan ke Cloudinary.
     */
    suspend fun uploadFromUrl(
        sourceUrl: String,
        folder: String,
        publicId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("folder", folder)
                .addFormDataPart("public_id", publicId)
                .addFormDataPart("file", sourceUrl)
                .build()
            execute(body)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cadangan jika Cloudinary gagal mengambil link: unduh gambar di HP, lalu unggah bytes-nya.
     */
    suspend fun downloadAndUpload(
        sourceUrl: String,
        folder: String,
        publicId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(sourceUrl).get().build()
            val (bytes, mimeType) = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("Gambar sumber tidak bisa diunduh (${response.code})")
                    )
                }
                val type = response.header("Content-Type")?.substringBefore(";") ?: "image/jpeg"
                (response.body?.bytes() ?: ByteArray(0)) to type
            }
            if (bytes.isEmpty()) return@withContext Result.failure(IOException("Gambar sumber kosong"))
            uploadBytesInternal(bytes, mimeType, folder, publicId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun uploadBytesInternal(
        bytes: ByteArray,
        mimeType: String,
        folder: String,
        publicId: String?
    ): Result<String> {
        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .addFormDataPart("folder", folder)
        if (!publicId.isNullOrBlank()) builder.addFormDataPart("public_id", publicId)
        builder.addFormDataPart(
            "file",
            "upload_${System.currentTimeMillis()}.$extension",
            bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        )
        return execute(builder.build())
    }

    private fun execute(body: MultipartBody): Result<String> {
        val request = Request.Builder().url(UPLOAD_URL).post(body).build()
        client.newCall(request).execute().use { response ->
            val bodyString = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = runCatching {
                    JSONObject(bodyString).optJSONObject("error")?.optString("message")
                }.getOrNull()
                return Result.failure(
                    CloudinaryUploadException(response.code, message?.ifBlank { null } ?: bodyString)
                )
            }
            val secureUrl = JSONObject(bodyString).optString("secure_url")
            return if (secureUrl.isBlank()) {
                Result.failure(IOException("secure_url tidak ditemukan pada respons Cloudinary"))
            } else {
                Result.success(secureUrl)
            }
        }
    }
}

class CloudinaryUploadException(val code: Int, message: String) :
    IOException("Upload Cloudinary gagal ($code): $message") {
    /** Unsigned upload tidak boleh menimpa public_id yang sudah ada */
    val isAlreadyExists: Boolean
        get() = message?.contains("already exists", ignoreCase = true) == true ||
            message?.contains("overwrite", ignoreCase = true) == true
}
