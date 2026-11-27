package com.example.litutur.data.repository

import com.example.litutur.data.model.ChildProfileEntity
import com.example.litutur.data.model.ParentMissionEntity
import com.example.litutur.data.model.ParentUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection = firestore.collection("users")

    fun currentUserId(): String? = auth.currentUser?.uid

    suspend fun login(email: String, password: String): Result<ParentUser> = withContext(Dispatchers.IO) {
        try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val uid = authResult.user?.uid ?: return@withContext Result.failure(Exception("Gagal mendapatkan ID pengguna"))
            val profileResult = getParentProfile(uid)
            if (profileResult.isSuccess) {
                profileResult
            } else {
                val fallback = ParentUser(
                    uid = uid,
                    name = authResult.user?.displayName ?: email.substringBefore("@"),
                    email = email.trim()
                )
                usersCollection.document(uid).set(fallback).await()
                Result.success(fallback)
            }
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseAuthError(e)))
        }
    }

    suspend fun registerParent(
        name: String,
        email: String,
        whatsapp: String,
        password: String
    ): Result<ParentUser> = withContext(Dispatchers.IO) {
        try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val uid = authResult.user?.uid ?: return@withContext Result.failure(Exception("Gagal membuat akun"))
            val parentUser = ParentUser(
                uid = uid,
                name = name.trim(),
                email = email.trim(),
                whatsapp = whatsapp.trim(),
                parentPin = "1234",
                createdAt = System.currentTimeMillis()
            )
            usersCollection.document(uid).set(parentUser).await()
            Result.success(parentUser)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseAuthError(e)))
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(mapFirebaseAuthError(e)))
        }
    }

    suspend fun getParentProfile(uid: String): Result<ParentUser> = withContext(Dispatchers.IO) {
        try {
            val snapshot = usersCollection.document(uid).get().await()
            val user = snapshot.toObject(ParentUser::class.java)
            if (user != null) {
                Result.success(user.copy(uid = uid))
            } else {
                Result.failure(Exception("Profil orang tua belum ditemukan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateParentProfile(
        uid: String,
        name: String? = null,
        whatsapp: String? = null,
        avatarUrl: String? = null,
        parentPin: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val updates = mutableMapOf<String, Any>()
            name?.let { updates["name"] = it.trim() }
            whatsapp?.let { updates["whatsapp"] = it.trim() }
            avatarUrl?.let { updates["avatarUrl"] = it }
            parentPin?.let { updates["parentPin"] = it }
            if (updates.isNotEmpty()) {
                usersCollection.document(uid).update(updates).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveChildProfile(
        uid: String,
        child: ChildProfileEntity
    ): Result<ChildProfileEntity> = withContext(Dispatchers.IO) {
        try {
            val childId = child.id.ifBlank { UUID.randomUUID().toString() }
            val initialInterests = if (child.interestScores.isEmpty()) {
                child.favoriteTopics.associateWith { 10 }
            } else {
                child.interestScores
            }
            val finalChild = child.copy(
                id = childId,
                interestScores = initialInterests
            )

            val childRef = usersCollection
                .document(uid)
                .collection("children")
                .document(childId)

            childRef.set(finalChild).await()

            val existingMissions = childRef.collection("missions").limit(1).get().await()
            if (existingMissions.isEmpty) {
                val batch = firestore.batch()
                defaultParentMissions().forEach { mission ->
                    val missionRef = childRef.collection("missions").document(mission.id)
                    batch.set(missionRef, mission)
                }
                batch.commit().await()
            }

            Result.success(finalChild)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChildren(uid: String): Result<List<ChildProfileEntity>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = usersCollection
                .document(uid)
                .collection("children")
                .get()
                .await()
            val children = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ChildProfileEntity::class.java)?.copy(id = doc.id)
            }.sortedBy { it.createdAt }
            Result.success(children)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    private fun defaultParentMissions(): List<ParentMissionEntity> = listOf(
        ParentMissionEntity(
            id = "m_bedtime_story",
            title = "Bacakan cerita sebelum tidur",
            description = "Luangkan 10-15 menit membacakan dongeng dengan intonasi ekspresif sebelum si kecil tidur.",
            category = "Bonding Orang Tua",
            pointsReward = 25
        ),
        ParentMissionEntity(
            id = "m_discuss_moral",
            title = "Diskusikan pesan moral cerita",
            description = "Tanyakan kepada anak pelajaran baik apa yang bisa dicontoh dari tokoh utama.",
            category = "Offline Bonding",
            pointsReward = 20
        ),
        ParentMissionEntity(
            id = "m_character_roleplay",
            title = "Tirukan suara tokoh bersama anak",
            description = "Ajak anak bermain peran menirukan suara hewan atau karakter dari cerita yang baru dibaca.",
            category = "Kreativitas",
            pointsReward = 20
        ),
        ParentMissionEntity(
            id = "m_new_vocab",
            title = "Kenalkan 3 kosakata baru hari ini",
            description = "Pilih 3 kata baru dari cerita dan gunakan dalam percakapan sehari-hari bersama anak.",
            category = "Literasi Dini",
            pointsReward = 15
        ),
        ParentMissionEntity(
            id = "m_praise_quiz",
            title = "Apresiasi hasil kuis pemahaman anak",
            description = "Berikan pujian hangat atas usaha anak menyelesaikan kuis cerita minggu ini.",
            category = "Motivasi",
            pointsReward = 20
        )
    )

    private fun mapFirebaseAuthError(e: Exception): String {
        val msg = e.localizedMessage.orEmpty()
        return when {
            msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                msg.contains("password is invalid", ignoreCase = true) ||
                msg.contains("no user record", ignoreCase = true) ->
                "Email atau kata sandi salah. Silakan periksa kembali."
            msg.contains("email address is already in use", ignoreCase = true) ->
                "Email sudah terdaftar. Silakan masuk menggunakan akun Anda."
            msg.contains("badly formatted", ignoreCase = true) ->
                "Format alamat email tidak valid."
            msg.contains("least 6 characters", ignoreCase = true) ->
                "Kata sandi minimal 6 karakter."
            msg.contains("network error", ignoreCase = true) ->
                "Koneksi internet bermasalah. Periksa jaringan Anda."
            else -> msg.ifBlank { "Terjadi kesalahan autentikasi. Coba lagi." }
        }
    }
}
