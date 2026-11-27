package com.example.litutur.data.remote

/**
 * Katalog ilustrasi bawaan aplikasi (di luar buku & stiker).
 * sourceUrl = link asli (sementara). Setelah sinkronisasi, gambar disajikan dari Cloudinary
 * melalui [com.example.litutur.data.repository.CloudinaryAssetSync.resolve].
 */
object LituturAssets {
    const val FOLDER_ILLUSTRATIONS = "litutur/illustrations"
    const val FOLDER_STICKERS = "litutur/stickers"

    data class UiAsset(val publicId: String, val sourceUrl: String)

    val DASHBOARD_MASCOT = UiAsset(
        publicId = "ui_dashboard_mascot",
        sourceUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBlZm93mKuue9mdTbnk8VDHYRFeeAWVUDUd9aKstzHy1Q7yIa5bCaETgeRTbyT28amYwl-_U8I4Rwrzp8QFpNwAQiDWQ79HVjj2KaVj3lnBLd74r50Rq41Gi5YD4CS7aGgJyGT3azocFD8WTJY2u63rhWldFqjMNiSbuesHAUDvjEApFe-YEoy2qJJCCykVWF1wKW1mCxWNMVeZsmiqus4k04PqeZZTVCc-210SdBMY"
    )

    val POHON_KISAH = UiAsset(
        publicId = "ui_pohon_kisah",
        sourceUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAYiGzFhozN3--zZ-Nx7pGkMUBPQ8JtkE_xWv8BkeiMWXwtoZrESEQYxJY4UCjGjypxSZUguZjKBgNqPxLwFxK5t-RaP_MYxz1J82QHSyryRaiiO0_tyd6xJY403ECiaOXoqOINE1s3chVCMOvzS30j8GN7vzUF3TvsuDXH1W2DWvdgOaYaOtxojHFRVfahe0F4gOLITGiQdINQm_DqwMI837KWhd2F6kpujnmsyf1E"
    )

    val KUIS_SELESAI_TROPHY = UiAsset(
        publicId = "ui_kuis_selesai_trofi",
        sourceUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBe4n2jzIROOHNosEZFY_unI3-bEkGGM1RYeJrf0VY8DWBhCGEL_e-VOkYCk9MemXubxJZFcAGES695VbeiLp1i_eY8Sz_WlpFUQH_MGY2MhwBVnYvF-1Bqaji-p3-OMAQRSd3ybEEW57chUqjG49qVqrTtpP2_OHuEbi9nFBQaYvi7ly9SRqdwuqPb5xas42-pV95RrnkkhoUZbCKpHqpJngGeo6X44Tnbztyl1RES"
    )

    val uiAssets: List<UiAsset> = listOf(DASHBOARD_MASCOT, POHON_KISAH, KUIS_SELESAI_TROPHY)
}
