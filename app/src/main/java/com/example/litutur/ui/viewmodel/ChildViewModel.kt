package com.example.litutur.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.litutur.data.model.CeritaBukuModel
import com.example.litutur.data.model.ChildProfileEntity
import com.example.litutur.data.model.HasilKuisModel
import com.example.litutur.data.model.ReadingLogEntity
import com.example.litutur.data.model.SoalKuisModel
import com.example.litutur.data.model.StikerModel
import com.example.litutur.data.repository.ChildRepository
import com.example.litutur.data.repository.TreeGrowthData
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil

/** Aturan gamifikasi harian Mode Anak */
object ChildRewards {
    const val WATER_TREE_POINTS = 5          // Siram pohon 1x sehari
    const val DAILY_MISSION_TARGET = 2       // Misi Hari Ini: baca 2 cerita
    const val DAILY_MISSION_POINTS = 20      // Hadiah Misi Hari Ini
    const val MYSTERY_BOX_COST = 25          // Harga Kotak Stiker Misterius (koin)
    const val FAVORITE_CATEGORY = "Favoritku"
}

/**
 * Status penyimpanan hasil kuis ke Firestore (ditampilkan di layar Kuis Selesai)
 */
enum class QuizSaveStatus {
    IDLE,
    SAVING,      // Batch sedang dikirim (offline: tersimpan di cache, menunggu sinkron)
    SAVED,       // Sudah dikonfirmasi server
    LOCAL_ONLY,  // Mode tamu / tanpa akun: hanya tersimpan di sesi ini
    FAILED       // Gagal disimpan (mis. izin Firestore ditolak)
}

/**
 * Ringkasan hasil kuis yang baru selesai: dipakai layar Kuis Selesai dan disinkronkan ke Mode Orang Tua.
 */
data class QuizCompletionResult(
    val bookId: String,
    val bookTitle: String,
    val category: String,
    val scorePercentage: Int,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val pointsEarned: Int,
    val durationSeconds: Int,
    val totalPointsAfter: Int,
    val newlyUnlockedStickers: List<StikerModel>,
    val nextLockedSticker: StikerModel?,
    val readingLog: ReadingLogEntity
)

/**
 * State UI untuk keseluruhan Fitur Anak (Membaca, Kuis, Gamifikasi Pohon & Stiker)
 */
data class ChildUiState(
    val isLoading: Boolean = false,
    val childProfile: ChildProfileEntity? = null,
    val allBooks: List<CeritaBukuModel> = emptyList(),
    val displayedBooks: List<CeritaBukuModel> = emptyList(),
    val selectedCategoryIndex: Int = 0,
    val categories: List<String> = listOf(
        "Semua Cerita", "Fabel", "Petualangan", "Dongeng", "Sains", ChildRewards.FAVORITE_CATEGORY
    ),
    // Progres anak (real-time dari Firestore)
    val readingLogs: List<ReadingLogEntity> = emptyList(),
    val storiesCount: Int = 0,
    val maxQuizScore: Int = 0,
    val isRealtimeSynced: Boolean = false,
    val todayKey: String = "",
    val storiesReadToday: Int = 0,
    // Kotak Stiker Misterius: stiker yang baru didapat (ditampilkan di dialog)
    val mysteryBoxReveal: StikerModel? = null,
    // Pesan singkat untuk Toast (dikonsumsi UI)
    val childMessage: String? = null,
    // Detail Buku Aktif
    val currentBook: CeritaBukuModel? = null,
    val currentChapterIndex: Int = 0,
    val readingStartedAt: Long = 0L,
    // Kuis Aktif
    val quizQuestions: List<SoalKuisModel> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswerId: String? = null,
    val correctAnswersCount: Int = 0,
    val isQuizFinished: Boolean = false,
    val lastCalculatedQuizScore: Int = 0,
    val lastCalculatedPoints: Int = 0,
    val lastQuizResult: QuizCompletionResult? = null,
    val quizSaveStatus: QuizSaveStatus = QuizSaveStatus.IDLE,
    // Gamifikasi
    val treeGrowth: TreeGrowthData = TreeGrowthData(1, "Tunas Kecil 🌱", 0.5f, 25, "Bibit Hijau"),
    val stickers: List<StikerModel> = emptyList()
) {
    val childPoints: Int
        get() = childProfile?.points ?: 0

    /** Saldo koin = total poin (XP) dikurangi koin yang sudah dibelanjakan. Pohon tetap memakai XP. */
    val availableCoins: Int
        get() = ((childProfile?.points ?: 0) - (childProfile?.coinsSpent ?: 0)).coerceAtLeast(0)

    val isWateredToday: Boolean
        get() = todayKey.isNotEmpty() && childProfile?.lastWateredDate == todayKey

    val isDailyMissionClaimed: Boolean
        get() = todayKey.isNotEmpty() && childProfile?.lastMissionClaimDate == todayKey

    val favoriteBookIds: List<String>
        get() = childProfile?.favoriteBookIds.orEmpty()
}

class ChildViewModel(
    private val childRepository: ChildRepository = ChildRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildUiState())
    val uiState: StateFlow<ChildUiState> = _uiState.asStateFlow()

    /** Buku mentah dari Firestore (urutan asli), disortir ulang setiap minat anak berubah */
    private var rawBooks: List<CeritaBukuModel> = emptyList()
    private var booksJob: Job? = null
    private var observeJob: Job? = null
    private var boundUid: String? = null
    private var boundChildId: String? = null

    /**
     * Menghubungkan Mode Anak ke profil anak aktif.
     * - Login: pasang listener real-time ke dokumen anak + reading_logs, sehingga poin, Pohon Kisah,
     *   dan Album Stiker otomatis ikut berubah setiap ada data baru di Firestore.
     * - Mode tamu (uid null): pakai data lokal dari Mode Orang Tua.
     */
    fun bindChild(
        uid: String?,
        child: ChildProfileEntity?,
        fallbackLogs: List<ReadingLogEntity> = emptyList()
    ) {
        val childId = child?.id?.takeIf { it.isNotBlank() }
        val canObserve = !uid.isNullOrBlank() && childId != null

        // Listener untuk anak yang sama sudah aktif -> tidak perlu dipasang ulang
        if (canObserve && uid == boundUid && childId == boundChildId && observeJob?.isActive == true) return

        observeJob?.cancel()
        observeJob = null
        boundUid = uid
        boundChildId = childId

        // Tampilkan data awal secepatnya (sebelum snapshot pertama tiba)
        applyChildState(child, fallbackLogs, synced = false)
        ensureBooksLoaded()

        if (canObserve) {
            observeJob = viewModelScope.launch {
                combine(
                    childRepository.observeChildProfile(uid!!, childId!!),
                    childRepository.observeReadingLogs(uid, childId)
                ) { profile, logs -> profile to logs }
                    .collect { (profile, logs) ->
                        // Dokumen belum ada / terhapus -> pertahankan profil terakhir
                        applyChildState(profile ?: _uiState.value.childProfile, logs, synced = true)
                    }
            }
        }
    }

    /**
     * Memuat buku dari Firestore (sekali), lalu urutkan sesuai minat anak aktif
     */
    private fun ensureBooksLoaded(forceReload: Boolean = false) {
        if (!forceReload && (rawBooks.isNotEmpty() || booksJob?.isActive == true)) {
            resortBooks()
            return
        }
        booksJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            rawBooks = childRepository.getBooks().getOrDefault(childRepository.getDefaultBooks())
            _uiState.update { it.copy(isLoading = false) }
            resortBooks()
        }
    }

    fun refreshBooks() = ensureBooksLoaded(forceReload = true)

    private fun resortBooks() {
        val child = _uiState.value.childProfile
        val sorted = if (child != null) {
            childRepository.sortBooksByInterest(
                books = rawBooks,
                interestScores = child.interestScores,
                favoriteTopics = child.favoriteTopics
            )
        } else {
            rawBooks
        }
        _uiState.update {
            it.copy(
                allBooks = sorted,
                // Pertahankan kategori yang sedang dipilih saat data dimuat ulang
                displayedBooks = filterBooks(sorted, it.categories, it.selectedCategoryIndex, it.favoriteBookIds)
            )
        }
    }

    /**
     * Hitung ulang status gamifikasi dari profil & riwayat baca terbaru
     */
    private fun applyChildState(
        child: ChildProfileEntity?,
        logs: List<ReadingLogEntity>,
        synced: Boolean
    ) {
        val previousInterest = _uiState.value.childProfile?.interestScores
        val points = child?.points ?: 0
        val storiesCount = logs.size
        val maxQuizScore = logs.maxOfOrNull { it.quizScore } ?: 0
        val startOfToday = startOfTodayMillis()

        _uiState.update {
            it.copy(
                childProfile = child,
                readingLogs = logs,
                storiesCount = storiesCount,
                maxQuizScore = maxQuizScore,
                isRealtimeSynced = synced,
                todayKey = todayKey(),
                storiesReadToday = logs.count { log -> log.completedAt >= startOfToday },
                treeGrowth = childRepository.calculateTreeGrowth(points),
                stickers = childRepository.evaluateStickersUnlock(
                    storiesCount, points, maxQuizScore, child?.purchasedStickerIds.orEmpty()
                ),
                // Daftar "Favoritku" ikut berubah saat favorit ditambah/dihapus
                displayedBooks = filterBooks(it.allBooks, it.categories, it.selectedCategoryIndex, child?.favoriteBookIds.orEmpty())
            )
        }

        // Minat baca berubah (mis. setelah kuis) -> rekomendasi buku ikut diurutkan ulang
        if (rawBooks.isNotEmpty() && previousInterest != child?.interestScores) {
            resortBooks()
        }
    }

    /**
     * Filter kategori buku di Dashboard Anak
     */
    fun selectCategory(index: Int) {
        val cats = _uiState.value.categories
        if (index !in cats.indices) return

        _uiState.update {
            it.copy(
                selectedCategoryIndex = index,
                displayedBooks = filterBooks(it.allBooks, cats, index, it.favoriteBookIds)
            )
        }
    }

    private fun filterBooks(
        books: List<CeritaBukuModel>,
        categories: List<String>,
        index: Int,
        favoriteIds: List<String>
    ): List<CeritaBukuModel> {
        val categoryName = categories.getOrNull(index)
        if (index == 0 || categoryName == null) return books
        if (categoryName == ChildRewards.FAVORITE_CATEGORY) {
            return books.filter { bookKey(it) in favoriteIds }
        }
        return books.filter {
            it.category.contains(categoryName, ignoreCase = true) ||
                categoryName.contains(it.category, ignoreCase = true)
        }
    }

    /**
     * Memilih buku cerita untuk dibaca (mereset bab, kuis, dan timer membaca)
     */
    fun selectBookForReading(bookTitle: String) {
        viewModelScope.launch {
            // Cari di daftar yang sudah dimuat dulu agar tidak query Firestore ulang
            val book = _uiState.value.allBooks.find { it.title.equals(bookTitle, ignoreCase = true) }
                ?: childRepository.getBookByTitle(bookTitle)
            _uiState.update {
                it.copy(
                    currentBook = book,
                    currentChapterIndex = 0,
                    readingStartedAt = System.currentTimeMillis(),
                    quizQuestions = book.quizQuestions,
                    currentQuestionIndex = 0,
                    selectedAnswerId = null,
                    correctAnswersCount = 0,
                    isQuizFinished = false,
                    quizSaveStatus = QuizSaveStatus.IDLE
                )
            }
        }
    }

    /**
     * Lanjut ke bab/chapter berikutnya
     */
    fun nextChapter(): Boolean {
        val book = _uiState.value.currentBook ?: return false
        val currentIdx = _uiState.value.currentChapterIndex
        return if (currentIdx + 1 < book.chapters.size) {
            _uiState.update { it.copy(currentChapterIndex = currentIdx + 1) }
            true
        } else {
            false // Sudah bab terakhir, lanjut ke kuis
        }
    }

    /**
     * Kembali ke bab sebelumnya. Return false jika sudah di bab pertama.
     */
    fun previousChapter(): Boolean {
        val currentIdx = _uiState.value.currentChapterIndex
        return if (currentIdx > 0) {
            _uiState.update { it.copy(currentChapterIndex = currentIdx - 1) }
            true
        } else {
            false
        }
    }

    /**
     * Memilih opsi jawaban kuis
     */
    fun selectQuizOption(optionId: String) {
        if (_uiState.value.isQuizFinished) return
        _uiState.update { it.copy(selectedAnswerId = optionId) }
    }

    /**
     * Kirim jawaban soal kuis saat ini.
     * - Belum soal terakhir: lanjut ke soal berikutnya, return null.
     * - Soal terakhir: hitung skor & XP, perbarui Pohon/Stiker seketika, simpan ke Firestore,
     *   lalu return [QuizCompletionResult]. Panggilan berikutnya (tap ganda) return null
     *   sehingga XP tidak pernah tercatat dua kali.
     */
    fun submitAnswerAndProceed(uid: String?): QuizCompletionResult? {
        val state = _uiState.value
        if (state.isQuizFinished) return null

        val book = state.currentBook ?: return null
        val questions = state.quizQuestions
        val currentIdx = state.currentQuestionIndex
        val currentQuestion = questions.getOrNull(currentIdx)
        if (currentQuestion != null && state.selectedAnswerId == null) return null

        var newCorrect = state.correctAnswersCount
        if (currentQuestion != null && state.selectedAnswerId == currentQuestion.correctAnswerId) {
            newCorrect++
        }

        val isFinished = currentIdx + 1 >= questions.size
        if (!isFinished) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = currentIdx + 1,
                    selectedAnswerId = null,
                    correctAnswersCount = newCorrect
                )
            }
            return null
        }

        // Problem Solving: Kalkulasi persentase skor dan reward poin gamifikasi
        val totalQ = questions.size.coerceAtLeast(1)
        val scorePercentage = ((newCorrect.toFloat() / totalQ.toFloat()) * 100).toInt()
        val bonusPoints = when {
            scorePercentage == 100 -> 25 // Sempurna +25 Poin
            scorePercentage >= 70 -> 15  // Baik +15 Poin
            else -> 10                  // Usaha +10 Poin
        }

        val now = System.currentTimeMillis()
        val durationSeconds = if (state.readingStartedAt > 0L) {
            ((now - state.readingStartedAt) / 1000L).toInt().coerceAtLeast(1)
        } else {
            book.durationMinutes * 60
        }
        val durationMinutes = ceil(durationSeconds / 60.0).toInt().coerceAtLeast(1)
        val category = book.category.ifBlank { "Lainnya" }

        val log = ReadingLogEntity(
            id = "quiz_${book.id.ifBlank { "buku" }}_$now",
            storyId = book.id,
            storyTitle = book.title,
            category = category,
            durationMinutes = durationMinutes,
            quizScore = scorePercentage,
            dayOfWeek = currentDayOfWeek(),
            moralLesson = book.moralCerita,
            synopsis = book.sinopsis,
            characters = emptyList(),
            quizQuestions = book.quizQuestions.map { it.questionText },
            completedAt = now
        )

        // Hitung pohon & stiker setelah kuis untuk mendeteksi stiker yang baru terbuka
        val oldProfile = state.childProfile
        val pointsAfter = (oldProfile?.points ?: 0) + bonusPoints
        val logsAfter = listOf(log) + state.readingLogs
        val stickersAfter = childRepository.evaluateStickersUnlock(
            storiesCount = logsAfter.size,
            totalPoints = pointsAfter,
            maxQuizScore = maxOf(state.maxQuizScore, scorePercentage),
            purchasedIds = oldProfile?.purchasedStickerIds.orEmpty()
        )
        val unlockedBefore = state.stickers.filter { it.isUnlocked }.map { it.id }.toSet()
        val newlyUnlocked = stickersAfter.filter { it.isUnlocked && it.id !in unlockedBefore }

        val result = QuizCompletionResult(
            bookId = book.id,
            bookTitle = book.title,
            category = category,
            scorePercentage = scorePercentage,
            correctAnswers = newCorrect,
            totalQuestions = questions.size,
            pointsEarned = bonusPoints,
            durationSeconds = durationSeconds,
            totalPointsAfter = pointsAfter,
            newlyUnlockedStickers = newlyUnlocked,
            nextLockedSticker = stickersAfter.firstOrNull { !it.isUnlocked },
            readingLog = log
        )

        val childId = oldProfile?.id?.takeIf { it.isNotBlank() }
        val willPersist = !uid.isNullOrBlank() && childId != null

        _uiState.update {
            it.copy(
                correctAnswersCount = newCorrect,
                isQuizFinished = true,
                lastCalculatedQuizScore = scorePercentage,
                lastCalculatedPoints = bonusPoints,
                lastQuizResult = result,
                quizSaveStatus = if (willPersist) QuizSaveStatus.SAVING else QuizSaveStatus.LOCAL_ONLY
            )
        }

        // Pembaruan optimistis: XP, Pohon Kisah & Stiker langsung berubah di layar.
        // Saat login, listener Firestore akan memancarkan nilai yang sama dari cache lokal.
        val optimisticProfile = oldProfile?.let { profile ->
            profile.copy(
                points = pointsAfter,
                minutesUsedToday = profile.minutesUsedToday + durationMinutes,
                interestScores = profile.interestScores +
                    (category to ((profile.interestScores[category] ?: 0) + 1))
            )
        }
        applyChildState(optimisticProfile, logsAfter, synced = _uiState.value.isRealtimeSynced)

        if (willPersist) {
            val quizResult = HasilKuisModel(
                childId = childId!!,
                bookId = book.id,
                bookTitle = book.title,
                scorePercentage = scorePercentage,
                correctAnswers = newCorrect,
                totalQuestions = questions.size,
                xpAwarded = bonusPoints,
                coinsAwarded = bonusPoints,
                timestamp = now
            )
            viewModelScope.launch {
                val saved = childRepository.saveQuizCompletion(
                    uid = uid!!,
                    childId = childId,
                    log = log,
                    result = quizResult,
                    category = category
                )
                _uiState.update {
                    // Abaikan jika anak sudah memulai buku/kuis lain
                    if (it.lastQuizResult?.readingLog?.id != log.id) it
                    else it.copy(
                        quizSaveStatus = if (saved.isSuccess) QuizSaveStatus.SAVED else QuizSaveStatus.FAILED
                    )
                }
            }
        }

        return result
    }

    // ---------------------------------------------------------------------
    // Aksi gamifikasi harian (Siram Pohon, Misi Hari Ini, Favorit, Kotak Stiker)
    // ---------------------------------------------------------------------

    /**
     * Siram Pohon: sekali sehari, +[ChildRewards.WATER_TREE_POINTS] poin, tersimpan di Firestore.
     */
    fun waterTree(uid: String?) {
        val state = _uiState.value
        val child = state.childProfile ?: return showMessage("Pilih profil anak dulu ya.")
        val today = todayKey()
        if (child.lastWateredDate == today) return showMessage("Pohon sudah disiram hari ini. Kembali besok ya! 🌸")

        val reward = ChildRewards.WATER_TREE_POINTS
        mutateChild(
            uid = uid,
            localUpdate = { it.copy(lastWateredDate = today, points = it.points + reward) },
            remoteUpdates = mapOf(
                "lastWateredDate" to today,
                "points" to FieldValue.increment(reward.toLong())
            ),
            successMessage = "Pohon disiram! +$reward Poin 💧"
        )
    }

    /**
     * Misi Hari Ini: baca [ChildRewards.DAILY_MISSION_TARGET] cerita hari ini,
     * lalu klaim +[ChildRewards.DAILY_MISSION_POINTS] poin (sekali sehari).
     */
    fun claimDailyMission(uid: String?) {
        val state = _uiState.value
        val child = state.childProfile ?: return showMessage("Pilih profil anak dulu ya.")
        val today = todayKey()
        if (child.lastMissionClaimDate == today) return showMessage("Hadiah misi hari ini sudah diklaim ✓")
        val remaining = ChildRewards.DAILY_MISSION_TARGET - state.storiesReadToday
        if (remaining > 0) return showMessage("Baca $remaining cerita lagi untuk menyelesaikan misi! 📚")

        val reward = ChildRewards.DAILY_MISSION_POINTS
        mutateChild(
            uid = uid,
            localUpdate = { it.copy(lastMissionClaimDate = today, points = it.points + reward) },
            remoteUpdates = mapOf(
                "lastMissionClaimDate" to today,
                "points" to FieldValue.increment(reward.toLong())
            ),
            successMessage = "Misi Hari Ini selesai! +$reward Poin 🎉"
        )
    }

    fun isFavorite(book: CeritaBukuModel?): Boolean =
        book != null && bookKey(book) in _uiState.value.favoriteBookIds

    /**
     * Tambah / hapus buku dari Favoritku (tersimpan di favoriteBookIds dokumen anak)
     */
    fun toggleFavorite(uid: String?, book: CeritaBukuModel?) {
        book ?: return
        val child = _uiState.value.childProfile ?: return showMessage("Pilih profil anak dulu ya.")
        val key = bookKey(book)
        val nowFavorite = key !in child.favoriteBookIds
        mutateChild(
            uid = uid,
            localUpdate = {
                it.copy(
                    favoriteBookIds = if (nowFavorite) (it.favoriteBookIds + key).distinct()
                    else it.favoriteBookIds - key
                )
            },
            remoteUpdates = mapOf(
                "favoriteBookIds" to if (nowFavorite) FieldValue.arrayUnion(key) else FieldValue.arrayRemove(key)
            ),
            successMessage = if (nowFavorite) "\"${book.title}\" masuk Favoritku ❤️" else "Dihapus dari Favoritku"
        )
    }

    /**
     * Kotak Stiker Misterius: bayar [ChildRewards.MYSTERY_BOX_COST] koin untuk membuka
     * 1 stiker acak yang masih terkunci. Koin dicatat di coinsSpent sehingga
     * pertumbuhan Pohon Kisah (berbasis XP) tidak ikut berkurang.
     */
    fun openMysteryBox(uid: String?) {
        val state = _uiState.value
        state.childProfile ?: return showMessage("Pilih profil anak dulu ya.")
        val locked = state.stickers.filter { !it.isUnlocked }
        if (locked.isEmpty()) return showMessage("Semua stiker sudah terkumpul! 🏆")
        val cost = ChildRewards.MYSTERY_BOX_COST
        if (state.availableCoins < cost) {
            return showMessage("Koin belum cukup. Butuh ${cost - state.availableCoins} koin lagi 🪙")
        }

        val prize = locked.random()
        mutateChild(
            uid = uid,
            localUpdate = {
                it.copy(
                    coinsSpent = it.coinsSpent + cost,
                    purchasedStickerIds = (it.purchasedStickerIds + prize.id).distinct()
                )
            },
            remoteUpdates = mapOf(
                "coinsSpent" to FieldValue.increment(cost.toLong()),
                "purchasedStickerIds" to FieldValue.arrayUnion(prize.id)
            ),
            successMessage = null
        )
        _uiState.update { it.copy(mysteryBoxReveal = prize.copy(isUnlocked = true)) }
    }

    fun dismissMysteryBoxReveal() {
        _uiState.update { it.copy(mysteryBoxReveal = null) }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(childMessage = null) }
    }

    private fun showMessage(message: String) {
        _uiState.update { it.copy(childMessage = message) }
    }

    /**
     * Pola umum: perbarui UI seketika (optimistis), lalu simpan ke Firestore.
     * Jika gagal, kembalikan ke state sebelumnya. Mode tamu hanya mengubah state lokal.
     */
    private fun mutateChild(
        uid: String?,
        localUpdate: (ChildProfileEntity) -> ChildProfileEntity,
        remoteUpdates: Map<String, Any>,
        successMessage: String?
    ) {
        val before = _uiState.value.childProfile ?: return
        val logs = _uiState.value.readingLogs
        applyChildState(localUpdate(before), logs, synced = _uiState.value.isRealtimeSynced)
        successMessage?.let { showMessage(it) }

        val childId = before.id.takeIf { it.isNotBlank() }
        if (uid.isNullOrBlank() || childId == null) return

        viewModelScope.launch {
            childRepository.updateChildFields(uid, childId, remoteUpdates).onFailure {
                // Kembalikan tampilan jika penyimpanan ditolak (mis. Security Rules)
                applyChildState(before, _uiState.value.readingLogs, synced = _uiState.value.isRealtimeSynced)
                showMessage("Gagal menyimpan, coba lagi ya.")
            }
        }
    }

    private fun bookKey(book: CeritaBukuModel): String = book.id.ifBlank { book.title }

    private fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)

    private fun startOfTodayMillis(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun currentDayOfWeek(): Int {
        val calDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return if (calDay == Calendar.SUNDAY) 7 else calDay - 1 // 1 = Senin .. 7 = Minggu
    }
}
