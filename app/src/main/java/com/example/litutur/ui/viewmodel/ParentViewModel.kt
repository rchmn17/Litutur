package com.example.litutur.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.litutur.data.model.ChildProfileEntity
import com.example.litutur.data.model.CommunityPostEntity
import com.example.litutur.data.model.ParentMissionEntity
import com.example.litutur.data.model.ParentUser
import com.example.litutur.data.model.PostCommentEntity
import com.example.litutur.data.model.ReadingLogEntity
import com.example.litutur.data.remote.CloudinaryUploader
import com.example.litutur.data.repository.AuthRepository
import com.example.litutur.data.repository.ParentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

data class LiteracyAnalysis(
    val dailyCounts: List<Int> = List(7) { 0 },
    val totalStoriesRead: Int = 0,
    val totalDurationMinutes: Int = 0,
    val averageQuizScore: Int = 0,
    val literacyPercent: Int = 0,
    val statusLabel: String = "Baru Memulai",
    val statusFeedback: String = "Belum ada riwayat bacaan minggu ini. Yuk mulai bacakan 1 cerita hari ini!",
    val recommendedCategory: String = "Fabel & Dongeng",
    val interestDistribution: Map<String, Float> = emptyMap()
)

data class BondingPromptData(
    val sourceStoryTitle: String,
    val sourceCategory: String,
    val isFromRecentReading: Boolean,
    val summaryText: String,
    val discussionQuestions: List<String>
)

data class AutoMilestoneOption(
    val id: String,
    val emoji: String,
    val title: String,
    val subtitle: String,
    val stats: String
)

data class ParentUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val parentUser: ParentUser? = null,
    val children: List<ChildProfileEntity> = emptyList(),
    val selectedChildIndex: Int = 0,
    val readingLogs: List<ReadingLogEntity> = emptyList(),
    val missions: List<ParentMissionEntity> = emptyList(),
    val literacyAnalysis: LiteracyAnalysis = LiteracyAnalysis(),
    val bondingPrompt: BondingPromptData? = null,
    val autoMilestones: List<AutoMilestoneOption> = emptyList(),
    val communityPosts: List<CommunityPostEntity> = emptyList(),
    val selectedPost: CommunityPostEntity? = null,
    val selectedPostComments: List<PostCommentEntity> = emptyList(),
    val lastCompletedStoryTitle: String = "Singa yang Baik Hati",
    val lastQuizScore: Int = 100,
    val lastEarnedPoints: Int = 25,
    val statusMessage: String? = null,
    val errorMessage: String? = null
) {
    val selectedChild: ChildProfileEntity?
        get() = children.getOrNull(selectedChildIndex) ?: children.firstOrNull()

    val completedMissionsCount: Int
        get() = missions.count { it.isCompleted }

    val totalMissionsCount: Int
        get() = missions.size

    val missionProgressFraction: Float
        get() = if (missions.isEmpty()) 0f else completedMissionsCount.toFloat() / missions.size.toFloat()

    val totalMissionPoints: Int
        get() = missions.filter { it.isCompleted }.sumOf { it.pointsReward }
}

class ParentViewModel(
    private val parentRepository: ParentRepository = ParentRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentUiState())
    val uiState: StateFlow<ParentUiState> = _uiState.asStateFlow()

    fun loadParentDashboard(parent: ParentUser?, initialChildren: List<ChildProfileEntity> = emptyList()) {
        val uid = parent?.uid ?: authRepository.currentUserId()
        _uiState.update {
            it.copy(
                parentUser = parent ?: it.parentUser,
                children = if (initialChildren.isNotEmpty()) initialChildren else it.children
            )
        }

        if (uid == null) {
            loadGuestPreviewData()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val loadedParent = parent ?: authRepository.getParentProfile(uid).getOrNull()
            val children = parentRepository.getChildren(uid).getOrDefault(initialChildren)
            val posts = parentRepository.getCommunityPosts().getOrDefault(emptyList())

            _uiState.update {
                it.copy(
                    parentUser = loadedParent,
                    children = children,
                    selectedChildIndex = it.selectedChildIndex.coerceIn(0, (children.size - 1).coerceAtLeast(0)),
                    communityPosts = posts
                )
            }

            val activeChild = children.getOrNull(_uiState.value.selectedChildIndex) ?: children.firstOrNull()
            if (activeChild != null) {
                loadChildDetails(uid, activeChild)
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectChild(index: Int) {
        val children = _uiState.value.children
        if (index !in children.indices) return
        _uiState.update { it.copy(selectedChildIndex = index) }
        val uid = _uiState.value.parentUser?.uid ?: authRepository.currentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            loadChildDetails(uid, children[index])
        }
    }

    private suspend fun loadChildDetails(uid: String, child: ChildProfileEntity) {
        val logs = parentRepository.getReadingLogs(uid, child.id).getOrDefault(emptyList())
        val missions = parentRepository.getMissions(uid, child.id).getOrDefault(emptyList())

        val analysis = withContext(Dispatchers.Default) {
            computeLiteracyAnalysis(child, logs)
        }
        val bondingPrompt = withContext(Dispatchers.Default) {
            generateBondingPrompt(child, logs)
        }
        val milestones = withContext(Dispatchers.Default) {
            generateAutoMilestones(child, analysis, missions)
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                readingLogs = logs,
                missions = missions,
                literacyAnalysis = analysis,
                bondingPrompt = bondingPrompt,
                autoMilestones = milestones
            )
        }
    }

    private fun computeLiteracyAnalysis(
        child: ChildProfileEntity,
        logs: List<ReadingLogEntity>
    ): LiteracyAnalysis {
        val dailyCounts = MutableList(7) { 0 }
        logs.forEach { log ->
            val idx = (log.dayOfWeek - 1).coerceIn(0, 6)
            dailyCounts[idx] = dailyCounts[idx] + 1
        }

        val totalStories = logs.size
        val totalMinutes = logs.sumOf { it.durationMinutes }
        val avgQuiz = if (logs.isNotEmpty()) logs.map { it.quizScore }.average().toInt() else 0

        val consistencyScore = ((totalStories.coerceAtMost(5) / 5f) * 100).toInt()
        val literacyPercent = if (totalStories == 0) 0 else ((consistencyScore * 0.5f) + (avgQuiz * 0.5f)).toInt()
        val activeDays = dailyCounts.count { it > 0 }

        val categoryCounts = mutableMapOf<String, Int>()
        child.favoriteTopics.forEach { topic -> categoryCounts[topic] = (categoryCounts[topic] ?: 0) + 1 }
        logs.forEach { log ->
            if (log.category.isNotBlank()) {
                categoryCounts[log.category] = (categoryCounts[log.category] ?: 0) + 2
            }
        }
        val totalInterestWeight = categoryCounts.values.sum().coerceAtLeast(1)
        val interestDistribution = categoryCounts.mapValues { (_, v) -> v.toFloat() / totalInterestWeight.toFloat() }
        val topCategory = categoryCounts.maxByOrNull { it.value }?.key ?: "Fabel & Dongeng"

        val (statusLabel, statusFeedback) = when {
            totalStories == 0 -> "Belum Ada Bacaan" to
                "${child.name} belum menyelesaikan cerita minggu ini. Coba bacakan cerita kategori $topCategory!"
            literacyPercent >= 80 -> "Sangat Baik" to
                "${child.name} aktif membaca ${totalStories}x ($activeDays hari) dengan pemahaman kuis $avgQuiz%. Pertahankan!"
            literacyPercent >= 55 -> "Baik" to
                "${child.name} rutin membaca ${totalStories}x minggu ini. Tambahkan 1-2 cerita $topCategory untuk tingkatkan fokus."
            else -> "Perlu Pendampingan" to
                "Aktivitas baca ${child.name} belum mencapai target mingguan. Disarankan mendampingi cerita bertema $topCategory."
        }

        return LiteracyAnalysis(
            dailyCounts = dailyCounts,
            totalStoriesRead = totalStories,
            totalDurationMinutes = totalMinutes,
            averageQuizScore = avgQuiz,
            literacyPercent = literacyPercent,
            statusLabel = statusLabel,
            statusFeedback = statusFeedback,
            recommendedCategory = topCategory,
            interestDistribution = interestDistribution
        )
    }

    private fun generateBondingPrompt(
        child: ChildProfileEntity,
        logs: List<ReadingLogEntity>
    ): BondingPromptData {
        val latest = logs.firstOrNull()
        return if (latest != null) {
            val charText = if (latest.characters.isNotEmpty()) {
                "Tokoh utama: ${latest.characters.joinToString(", ")}."
            } else {
                ""
            }
            val questions = latest.quizQuestions.ifEmpty {
                listOf(
                    "Menurut ${child.name}, bagian mana dari cerita \"${latest.storyTitle}\" yang paling seru?",
                    "Kalau ${child.name} jadi tokoh utamanya, apa yang akan dilakukan?",
                    "Pelajaran baik apa yang bisa kita praktikkan hari ini dari cerita ini?"
                )
            }
            BondingPromptData(
                sourceStoryTitle = latest.storyTitle,
                sourceCategory = latest.category.ifBlank { "Dongeng" },
                isFromRecentReading = true,
                summaryText = latest.synopsis.ifBlank {
                    "Cerita bertema ${latest.category} tentang ${latest.moralLesson.ifBlank { "kebaikan dan keberanian" }}. $charText"
                },
                discussionQuestions = questions
            )
        } else {
            val favTopic = child.favoriteTopics.firstOrNull() ?: "Fabel & Dongeng"
            BondingPromptData(
                sourceStoryTitle = "Rekomendasi: Kancil & Sahabat Hutan",
                sourceCategory = favTopic,
                isFromRecentReading = false,
                summaryText = "${child.name} belum menyelesaikan cerita minggu ini. Gunakan cerita populer bertema $favTopic ini untuk memancing rasa ingin tahu sebelum membaca.",
                discussionQuestions = listOf(
                    "Menurut ${child.name}, hewan apa yang paling pintar di hutan?",
                    "Yuk kita tebak bersama, apa yang akan terjadi kalau Kancil bertemu teman baru?",
                    "Cerita bertema $favTopic seperti apa yang ingin ${child.name} baca malam ini?"
                )
            )
        }
    }

    private fun generateAutoMilestones(
        child: ChildProfileEntity,
        analysis: LiteracyAnalysis,
        missions: List<ParentMissionEntity>
    ): List<AutoMilestoneOption> {
        val doneMissions = missions.count { it.isCompleted }
        val totalMissions = missions.size.coerceAtLeast(1)
        return listOf(
            AutoMilestoneOption(
                id = "m_stories",
                emoji = "📚",
                title = "${analysis.totalStoriesRead} Cerita Selesai Minggu Ini",
                subtitle = "${child.name} (${child.ageRange}) • Status: ${analysis.statusLabel}",
                stats = "${analysis.totalDurationMinutes} mnt • Kuis ${analysis.averageQuizScore}%"
            ),
            AutoMilestoneOption(
                id = "m_missions",
                emoji = "✅",
                title = "$doneMissions/$totalMissions Misi Pendampingan Selesai",
                subtitle = "Komitmen bonding literasi bersama ${child.name}",
                stats = "+${missions.filter { it.isCompleted }.sumOf { it.pointsReward }} Poin Bonding"
            ),
            AutoMilestoneOption(
                id = "m_interest",
                emoji = "🌟",
                title = "Minat Utama: ${analysis.recommendedCategory}",
                subtitle = "Pemetaan bakat literasi ${child.name}",
                stats = "Skor Literasi ${analysis.literacyPercent}%"
            )
        )
    }

    /**
     * Integrasi Mode Anak -> Firestore -> Mode Orang Tua:
     * Dipanggil saat anak menyelesaikan kuis setelah membaca cerita.
     */
    fun completeChildReadingAndQuiz(
        storyTitle: String,
        quizScore: Int,
        pointsEarned: Int,
        durationMinutes: Int = 8
    ) {
        val category = when {
            storyTitle.contains("Singa", ignoreCase = true) ||
                storyTitle.contains("Kelinci", ignoreCase = true) ||
                storyTitle.contains("Kancil", ignoreCase = true) -> "Fabel & Dongeng"
            storyTitle.contains("Bintang", ignoreCase = true) -> "Sains & Alam"
            else -> "Petualangan"
        }

        val moralLesson = when {
            storyTitle.contains("Singa", ignoreCase = true) -> "Rasa peduli dan saling tolong-menolong"
            storyTitle.contains("Kelinci", ignoreCase = true) -> "Ketekunan dan tidak meremehkan orang lain"
            storyTitle.contains("Bintang", ignoreCase = true) -> "Rasa ingin tahu terhadap alam semesta"
            else -> "Kecerdikan dan keberanian menghadapi tantangan"
        }

        val calDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val dayOfWeek = if (calDay == Calendar.SUNDAY) 7 else calDay - 1

        val log = ReadingLogEntity(
            id = "child_${System.currentTimeMillis()}",
            storyTitle = storyTitle,
            category = category,
            durationMinutes = durationMinutes,
            quizScore = quizScore,
            dayOfWeek = dayOfWeek,
            moralLesson = moralLesson,
            synopsis = "Cerita \"$storyTitle\" bertema $category yang mengajarkan $moralLesson.",
            characters = listOf("Tokoh Utama", "Sahabat Rimba"),
            quizQuestions = listOf(
                "Apa yang dilakukan tokoh utama di cerita \"$storyTitle\" saat melihat temannya kesulitan?",
                "Pelajaran baik apa yang bisa kita tiru dari \"$storyTitle\" hari ini?"
            )
        )

        val child = _uiState.value.selectedChild
        val uid = _uiState.value.parentUser?.uid ?: authRepository.currentUserId()

        _uiState.update {
            it.copy(
                lastCompletedStoryTitle = storyTitle,
                lastQuizScore = quizScore,
                lastEarnedPoints = pointsEarned
            )
        }

        if (child != null) {
            val updatedChild = child.copy(
                points = child.points + pointsEarned,
                minutesUsedToday = child.minutesUsedToday + durationMinutes
            )
            val updatedChildren = _uiState.value.children.map {
                if (it.id == child.id) updatedChild else it
            }
            val updatedLogs = listOf(log) + _uiState.value.readingLogs
            val analysis = computeLiteracyAnalysis(updatedChild, updatedLogs)
            val bonding = generateBondingPrompt(updatedChild, updatedLogs)
            val milestones = generateAutoMilestones(updatedChild, analysis, _uiState.value.missions)

            _uiState.update {
                it.copy(
                    children = updatedChildren,
                    readingLogs = updatedLogs,
                    literacyAnalysis = analysis,
                    bondingPrompt = bonding,
                    autoMilestones = milestones
                )
            }

            if (!uid.isNullOrBlank()) {
                viewModelScope.launch {
                    parentRepository.addReadingLog(uid, child.id, log, pointsEarned).onSuccess {
                        loadChildDetails(uid, updatedChild)
                    }
                }
            }
        }
    }

    fun toggleMission(mission: ParentMissionEntity) {
        val uid = _uiState.value.parentUser?.uid ?: authRepository.currentUserId()
        val child = _uiState.value.selectedChild
        val newCompleted = !mission.isCompleted
        val dayName = if (newCompleted) currentIndonesianDayName() else null

        val updatedMissions = _uiState.value.missions.map {
            if (it.id == mission.id) it.copy(isCompleted = newCompleted, completedDay = dayName) else it
        }
        _uiState.update { state ->
            state.copy(
                missions = updatedMissions,
                autoMilestones = child?.let { generateAutoMilestones(it, state.literacyAnalysis, updatedMissions) }
                    ?: state.autoMilestones
            )
        }

        if (uid != null && child != null) {
            viewModelScope.launch {
                parentRepository.toggleMission(uid, child.id, mission.id, newCompleted, dayName)
            }
        }
    }

    fun addCustomMission(title: String, description: String, category: String = "Misi Kustom") {
        if (title.isBlank()) return
        val uid = _uiState.value.parentUser?.uid ?: authRepository.currentUserId()
        val child = _uiState.value.selectedChild
        val newMission = ParentMissionEntity(
            title = title.trim(),
            description = description.trim().ifBlank { "Aktivitas pendampingan literasi khusus bersama anak." },
            category = category,
            pointsReward = 20,
            isCustom = true
        )

        if (uid == null || child == null) {
            val localMission = newMission.copy(id = "local_${System.currentTimeMillis()}")
            _uiState.update { it.copy(missions = it.missions + localMission) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = parentRepository.addCustomMission(uid, child.id, newMission)
            result.fold(
                onSuccess = { saved ->
                    val updated = _uiState.value.missions + saved
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            missions = updated,
                            statusMessage = "Misi baru berhasil ditambahkan!"
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = err.message) }
                }
            )
        }
    }

    fun recordReadingSession(
        storyTitle: String,
        category: String,
        durationMinutes: Int,
        quizScore: Int,
        moralLesson: String = "Keberanian dan saling menolong"
    ) {
        val uid = _uiState.value.parentUser?.uid ?: authRepository.currentUserId()
        val child = _uiState.value.selectedChild ?: return

        val calDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val dayOfWeek = if (calDay == Calendar.SUNDAY) 7 else calDay - 1

        val log = ReadingLogEntity(
            id = "manual_${System.currentTimeMillis()}",
            storyTitle = storyTitle,
            category = category,
            durationMinutes = durationMinutes,
            quizScore = quizScore,
            dayOfWeek = dayOfWeek,
            moralLesson = moralLesson,
            synopsis = "Cerita $storyTitle bertema $category yang mengajarkan $moralLesson.",
            characters = listOf("Tokoh Utama", "Sahabat Kecil"),
            quizQuestions = listOf(
                "Apa hal paling berani yang dilakukan tokoh di \"$storyTitle\"?",
                "Pelajaran apa yang bisa kita tiru dari cerita \"$storyTitle\"?"
            )
        )

        if (uid.isNullOrBlank()) {
            val updatedLogs = listOf(log) + _uiState.value.readingLogs
            val analysis = computeLiteracyAnalysis(child, updatedLogs)
            val bonding = generateBondingPrompt(child, updatedLogs)
            val milestones = generateAutoMilestones(child, analysis, _uiState.value.missions)
            _uiState.update {
                it.copy(
                    readingLogs = updatedLogs,
                    literacyAnalysis = analysis,
                    bondingPrompt = bonding,
                    autoMilestones = milestones,
                    statusMessage = "Aktivitas baca \"$storyTitle\" tercatat!"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            parentRepository.addReadingLog(uid, child.id, log).fold(
                onSuccess = {
                    loadChildDetails(uid, child)
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            statusMessage = "Aktivitas baca \"$storyTitle\" tercatat!"
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = err.message) }
                }
            )
        }
    }

    fun refreshCommunityPosts() {
        viewModelScope.launch {
            val posts = parentRepository.getCommunityPosts().getOrDefault(emptyList())
            _uiState.update { it.copy(communityPosts = posts) }
        }
    }

    fun createCommunityPost(
        caption: String,
        selectedMilestone: AutoMilestoneOption?,
        bookTitle: String?,
        photoUri: Uri?,
        context: Context,
        onSuccess: () -> Unit
    ) {
        if (caption.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Tulis cerita atau tips terlebih dahulu.") }
            return
        }

        val parent = _uiState.value.parentUser
        val uid = parent?.uid ?: authRepository.currentUserId()
        if (uid == null) {
            _uiState.update { it.copy(errorMessage = "Silakan masuk ke akun untuk memposting ke komunitas.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            var uploadedImageUrl = ""
            if (photoUri != null) {
                val uploadRes = CloudinaryUploader.uploadImage(
                    context = context,
                    imageUri = photoUri,
                    folder = "litutur/community"
                )
                uploadRes.fold(
                    onSuccess = { url -> uploadedImageUrl = url },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                errorMessage = "Gagal mengunggah foto: ${err.message}"
                            )
                        }
                        return@launch
                    }
                )
            }

            val child = _uiState.value.selectedChild
            val childInfo = if (child != null) "${child.name} (${child.ageRange})" else "Orang Tua Litutur"

            val post = CommunityPostEntity(
                authorUid = uid,
                authorName = parent?.name?.ifBlank { "Orang Tua Litutur" } ?: "Orang Tua Litutur",
                authorTitle = "Orang Tua Aktif",
                authorAvatarUrl = parent?.avatarUrl.orEmpty(),
                childInfo = childInfo,
                caption = caption.trim(),
                imageUrl = uploadedImageUrl,
                milestoneTitle = selectedMilestone?.title,
                milestoneSubtitle = selectedMilestone?.subtitle,
                milestoneStats = selectedMilestone?.stats,
                bookTitle = bookTitle?.takeIf { it.isNotBlank() },
                bookTag = if (!bookTitle.isNullOrBlank()) child?.favoriteTopics?.firstOrNull() ?: "Dongeng" else null
            )

            parentRepository.createCommunityPost(post).fold(
                onSuccess = { created ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            communityPosts = listOf(created) + it.communityPosts,
                            statusMessage = "Postingan berhasil dibagikan!"
                        )
                    }
                    onSuccess()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = err.message ?: "Gagal membuat postingan."
                        )
                    }
                }
            )
        }
    }

    fun toggleLikePost(post: CommunityPostEntity) {
        val uid = _uiState.value.parentUser?.uid ?: authRepository.currentUserId() ?: return
        val currentlyLiked = post.likedBy.contains(uid)
        val newLikedBy = if (currentlyLiked) post.likedBy - uid else post.likedBy + uid
        val newLikesCount = (post.likesCount + if (currentlyLiked) -1 else 1).coerceAtLeast(0)

        val updatedPost = post.copy(likedBy = newLikedBy, likesCount = newLikesCount)
        _uiState.update { state ->
            state.copy(
                communityPosts = state.communityPosts.map { if (it.id == post.id) updatedPost else it },
                selectedPost = if (state.selectedPost?.id == post.id) updatedPost else state.selectedPost
            )
        }

        viewModelScope.launch {
            parentRepository.togglePostLike(post.id, uid, currentlyLiked)
        }
    }

    fun openPostDetail(post: CommunityPostEntity) {
        _uiState.update { it.copy(selectedPost = post, selectedPostComments = emptyList()) }
        viewModelScope.launch {
            val comments = parentRepository.getComments(post.id).getOrDefault(emptyList())
            _uiState.update { it.copy(selectedPostComments = comments) }
        }
    }

    fun addComment(postId: String, text: String) {
        if (text.isBlank()) return
        val parent = _uiState.value.parentUser
        val uid = parent?.uid ?: authRepository.currentUserId() ?: return

        val comment = PostCommentEntity(
            authorUid = uid,
            authorName = parent?.name?.ifBlank { "Orang Tua Litutur" } ?: "Orang Tua Litutur",
            authorBadge = "Orang Tua",
            commentText = text.trim()
        )

        viewModelScope.launch {
            parentRepository.addComment(postId, comment).fold(
                onSuccess = { saved ->
                    _uiState.update { state ->
                        val updatedPosts = state.communityPosts.map { p ->
                            if (p.id == postId) p.copy(commentsCount = p.commentsCount + 1) else p
                        }
                        val updatedSelected = state.selectedPost?.let { p ->
                            if (p.id == postId) p.copy(commentsCount = p.commentsCount + 1) else p
                        }
                        state.copy(
                            communityPosts = updatedPosts,
                            selectedPost = updatedSelected,
                            selectedPostComments = state.selectedPostComments + saved
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(errorMessage = err.message) }
                }
            )
        }
    }

    fun updateParentPin(newPin: String) {
        if (newPin.length != 4 || !newPin.all { it.isDigit() }) {
            _uiState.update { it.copy(errorMessage = "PIN harus terdiri dari 4 angka.") }
            return
        }
        val uid = _uiState.value.parentUser?.uid ?: authRepository.currentUserId() ?: return
        viewModelScope.launch {
            authRepository.updateParentProfile(uid = uid, parentPin = newPin).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            parentUser = it.parentUser?.copy(parentPin = newPin),
                            statusMessage = "PIN Orang Tua berhasil diperbarui."
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(errorMessage = err.message) }
                }
            )
        }
    }

    fun updateChildDailyLimit(limitMinutes: Int) {
        val uid = _uiState.value.parentUser?.uid ?: authRepository.currentUserId() ?: return
        val child = _uiState.value.selectedChild ?: return
        viewModelScope.launch {
            parentRepository.updateChildDailyLimit(uid, child.id, limitMinutes).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            children = state.children.map {
                                if (it.id == child.id) it.copy(dailyLimitMinutes = limitMinutes) else it
                            },
                            statusMessage = "Batas waktu layar ${child.name} diubah menjadi $limitMinutes menit."
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(errorMessage = err.message) }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }

    private fun currentIndonesianDayName(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Senin"
            Calendar.TUESDAY -> "Selasa"
            Calendar.WEDNESDAY -> "Rabu"
            Calendar.THURSDAY -> "Kamis"
            Calendar.FRIDAY -> "Jumat"
            Calendar.SATURDAY -> "Sabtu"
            else -> "Minggu"
        }
    }

    private fun loadGuestPreviewData() {
        val guestChild = ChildProfileEntity(
            id = "guest_child",
            name = "Kayla",
            ageRange = "5-6 Thn",
            age = 6,
            favoriteTopics = listOf("Hewan", "Fabel & Dongeng", "Petualangan"),
            dailyLimitMinutes = 60,
            minutesUsedToday = 25,
            points = 85
        )
        val sampleLogs = listOf(
            ReadingLogEntity(
                id = "g1",
                storyTitle = "Kancil & Buaya",
                category = "Hewan",
                durationMinutes = 12,
                quizScore = 90,
                dayOfWeek = 1,
                moralLesson = "Kecerdikan mengalahkan kekuatan",
                synopsis = "Kancil menggunakan akal sehatnya untuk menyeberangi sungai dengan aman.",
                characters = listOf("Kancil", "Buaya")
            ),
            ReadingLogEntity(
                id = "g2",
                storyTitle = "Singa yang Baik Hati",
                category = "Fabel & Dongeng",
                durationMinutes = 15,
                quizScore = 100,
                dayOfWeek = 3,
                moralLesson = "Saling menolong tanpa memandang ukuran",
                synopsis = "Seekor singa melepaskan tikus kecil, dan kelak tikus itu membalas budi.",
                characters = listOf("Singa", "Tikus Kecil")
            )
        )
        val sampleMissions = listOf(
            ParentMissionEntity("gm1", "Bacakan cerita sebelum tidur", "10-15 menit sebelum tidur", "Bonding Orang Tua", 20, true, "Senin"),
            ParentMissionEntity("gm2", "Diskusikan pesan moral cerita", "Tanyakan tokoh favorit anak", "Offline Bonding", 20, true, "Rabu"),
            ParentMissionEntity("gm3", "Tirukan suara tokoh bersama anak", "Bermain peran bersama si kecil", "Kreativitas", 25, false, null)
        )
        val analysis = computeLiteracyAnalysis(guestChild, sampleLogs)
        val bonding = generateBondingPrompt(guestChild, sampleLogs)
        val milestones = generateAutoMilestones(guestChild, analysis, sampleMissions)
        _uiState.update {
            it.copy(
                isLoading = false,
                parentUser = ParentUser(uid = "", name = "Mode Tamu", email = "tamu@litutur.id"),
                children = listOf(guestChild),
                readingLogs = sampleLogs,
                missions = sampleMissions,
                literacyAnalysis = analysis,
                bondingPrompt = bonding,
                autoMilestones = milestones
            )
        }
    }
}
