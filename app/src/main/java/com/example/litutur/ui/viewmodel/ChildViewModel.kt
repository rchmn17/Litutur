package com.example.litutur.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.litutur.data.model.CeritaBukuModel
import com.example.litutur.data.model.ChapterModel
import com.example.litutur.data.model.ChildProfileEntity
import com.example.litutur.data.model.SoalKuisModel
import com.example.litutur.data.model.StikerModel
import com.example.litutur.data.repository.ChildRepository
import com.example.litutur.data.repository.TreeGrowthData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State UI untuk keseluruhan Fitur Anak (Membaca, Kuis, Gamifikasi Pohon & Stiker)
 */
data class ChildUiState(
    val isLoading: Boolean = false,
    val childProfile: ChildProfileEntity? = null,
    val allBooks: List<CeritaBukuModel> = emptyList(),
    val displayedBooks: List<CeritaBukuModel> = emptyList(),
    val selectedCategoryIndex: Int = 0,
    val categories: List<String> = listOf("Semua Cerita", "Fabel", "Petualangan", "Dongeng", "Sains"),
    // Detail Buku Aktif
    val currentBook: CeritaBukuModel? = null,
    val currentChapterIndex: Int = 0,
    // Kuis Aktif
    val quizQuestions: List<SoalKuisModel> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswerId: String? = null,
    val correctAnswersCount: Int = 0,
    val lastCalculatedQuizScore: Int = 0,
    val lastCalculatedPoints: Int = 0,
    // Gamifikasi
    val treeGrowth: TreeGrowthData = TreeGrowthData(1, "Tunas Kecil 🌱", 0.5f, 25, "Bibit Hijau"),
    val stickers: List<StikerModel> = emptyList()
)

class ChildViewModel(
    private val childRepository: ChildRepository = ChildRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildUiState())
    val uiState: StateFlow<ChildUiState> = _uiState.asStateFlow()

    /**
     * Memuat buku dan mengurutkan berdasarkan minat anak aktif
     */
    fun loadChildHome(child: ChildProfileEntity?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, childProfile = child) }
            val books = childRepository.getBooks().getOrDefault(childRepository.getDefaultBooks())

            val sortedBooks = if (child != null) {
                childRepository.sortBooksByInterest(
                    books = books,
                    interestScores = child.interestScores,
                    favoriteTopics = child.favoriteTopics
                )
            } else {
                books
            }

            val tree = childRepository.calculateTreeGrowth(child?.points ?: 0)
            val stickers = childRepository.evaluateStickersUnlock(
                storiesCount = 0,
                totalPoints = child?.points ?: 0,
                maxQuizScore = 100
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    allBooks = sortedBooks,
                    displayedBooks = sortedBooks,
                    treeGrowth = tree,
                    stickers = stickers
                )
            }
        }
    }

    /**
     * Filter kategori buku di Dashboard Anak
     */
    fun selectCategory(index: Int) {
        val cats = _uiState.value.categories
        if (index !in cats.indices) return
        val categoryName = cats[index]

        val filtered = if (index == 0) {
            _uiState.value.allBooks
        } else {
            _uiState.value.allBooks.filter {
                it.category.contains(categoryName, ignoreCase = true) ||
                    categoryName.contains(it.category, ignoreCase = true)
            }
        }

        _uiState.update {
            it.copy(
                selectedCategoryIndex = index,
                displayedBooks = filtered
            )
        }
    }

    /**
     * Memilih buku cerita untuk dibaca
     */
    fun selectBookForReading(bookTitle: String) {
        viewModelScope.launch {
            val book = childRepository.getBookByTitle(bookTitle)
            _uiState.update {
                it.copy(
                    currentBook = book,
                    currentChapterIndex = 0,
                    quizQuestions = book.quizQuestions,
                    currentQuestionIndex = 0,
                    selectedAnswerId = null,
                    correctAnswersCount = 0
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
     * Memilih opsi jawaban kuis
     */
    fun selectQuizOption(optionId: String) {
        _uiState.update { it.copy(selectedAnswerId = optionId) }
    }

    /**
     * Kirim jawaban soal kuis saat ini dan evaluasi skor
     * Return: pair(isComplete: Boolean, pair(score: Int, points: Int))
     */
    fun submitAnswerAndProceed(): Pair<Boolean, Pair<Int, Int>> {
        val state = _uiState.value
        val questions = state.quizQuestions
        val currentIdx = state.currentQuestionIndex
        val currentQuestion = questions.getOrNull(currentIdx)

        var newCorrect = state.correctAnswersCount
        if (currentQuestion != null && state.selectedAnswerId == currentQuestion.correctAnswerId) {
            newCorrect++
        }

        val totalQ = questions.size.coerceAtLeast(1)
        val isFinished = currentIdx + 1 >= questions.size

        if (isFinished) {
            // Problem Solving: Kalkulasi persentase skor dan reward poin gamifikasi
            val scorePercentage = ((newCorrect.toFloat() / totalQ.toFloat()) * 100).toInt()
            val bonusPoints = when {
                scorePercentage == 100 -> 25 // Sempurna +25 Poin
                scorePercentage >= 70 -> 15  // Baik +15 Poin
                else -> 10                  // Usaha +10 Poin
            }

            _uiState.update {
                it.copy(
                    correctAnswersCount = newCorrect,
                    lastCalculatedQuizScore = scorePercentage,
                    lastCalculatedPoints = bonusPoints
                )
            }
            return Pair(true, Pair(scorePercentage, bonusPoints))
        } else {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = currentIdx + 1,
                    selectedAnswerId = null,
                    correctAnswersCount = newCorrect
                )
            }
            return Pair(false, Pair(0, 0))
        }
    }

    /**
     * Memperbarui status gamifikasi (Pohon & Stiker) setelah aktivitas membaca selesai
     */
    fun updateGamification(totalPoints: Int, storiesCount: Int, quizScore: Int) {
        val tree = childRepository.calculateTreeGrowth(totalPoints)
        val stickers = childRepository.evaluateStickersUnlock(storiesCount, totalPoints, quizScore)
        _uiState.update {
            it.copy(
                treeGrowth = tree,
                stickers = stickers
            )
        }
    }
}
