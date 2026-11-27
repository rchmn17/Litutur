package com.example.litutur.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.litutur.data.repository.CloudinaryAssetSync

/**
 * Mengembalikan link Cloudinary untuk ilustrasi bawaan jika sudah dipindahkan,
 * atau link aslinya jika belum. Otomatis berubah begitu sinkronisasi selesai.
 */
@Composable
fun rememberAssetUrl(sourceUrl: String): String {
    val assets by CloudinaryAssetSync.state.collectAsState()
    return assets.bySource[sourceUrl] ?: sourceUrl
}
