package com.example.litutur.data.repository

import com.example.litutur.data.model.ChildProfileEntity
import com.example.litutur.data.model.CommunityPostEntity
import com.example.litutur.data.model.ParentMissionEntity
import com.example.litutur.data.model.PostCommentEntity
import com.example.litutur.data.model.ReadingLogEntity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class ParentRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCol = firestore.collection("users")
    private val communityCol = firestore.collection("community_posts")

    suspend fun getChildren(uid: String): Result<List<ChildProfileEntity>> = withContext(Dispatchers.IO) {
        try {
            val snap = usersCol.document(uid).collection("children").get().await()
            val list = snap.documents.mapNotNull { doc ->
                doc.toObject(ChildProfileEntity::class.java)?.copy(id = doc.id)
            }.sortedBy { it.createdAt }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReadingLogs(uid: String, childId: String): Result<List<ReadingLogEntity>> = withContext(Dispatchers.IO) {
        try {
            val snap = usersCol.document(uid)
                .collection("children").document(childId)
                .collection("reading_logs")
                .orderBy("completedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val logs = snap.documents.mapNotNull { doc ->
                doc.toObject(ReadingLogEntity::class.java)?.copy(id = doc.id)
            }
            Result.success(logs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addReadingLog(
        uid: String,
        childId: String,
        log: ReadingLogEntity
    ): Result<ReadingLogEntity> = withContext(Dispatchers.IO) {
        try {
            val logId = log.id.ifBlank { UUID.randomUUID().toString() }
            val finalLog = log.copy(id = logId)
            val childRef = usersCol.document(uid).collection("children").document(childId)
            childRef.collection("reading_logs").document(logId).set(finalLog).await()

            // Also increment child's minutesUsedToday and points
            childRef.update(
                mapOf(
                    "minutesUsedToday" to FieldValue.increment(finalLog.durationMinutes.toLong()),
                    "points" to FieldValue.increment(15L)
                )
            ).await()

            Result.success(finalLog)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMissions(uid: String, childId: String): Result<List<ParentMissionEntity>> = withContext(Dispatchers.IO) {
        try {
            val snap = usersCol.document(uid)
                .collection("children").document(childId)
                .collection("missions")
                .get()
                .await()
            val missions = snap.documents.mapNotNull { doc ->
                doc.toObject(ParentMissionEntity::class.java)?.copy(id = doc.id)
            }.sortedBy { it.createdAt }
            Result.success(missions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleMission(
        uid: String,
        childId: String,
        missionId: String,
        isCompleted: Boolean,
        completedDay: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val updates = mutableMapOf<String, Any?>(
                "isCompleted" to isCompleted,
                "completedDay" to completedDay
            )
            usersCol.document(uid)
                .collection("children").document(childId)
                .collection("missions").document(missionId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addCustomMission(
        uid: String,
        childId: String,
        mission: ParentMissionEntity
    ): Result<ParentMissionEntity> = withContext(Dispatchers.IO) {
        try {
            val id = mission.id.ifBlank { "custom_${UUID.randomUUID()}" }
            val finalMission = mission.copy(id = id, isCustom = true)
            usersCol.document(uid)
                .collection("children").document(childId)
                .collection("missions").document(id)
                .set(finalMission)
                .await()
            Result.success(finalMission)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateChildDailyLimit(
        uid: String,
        childId: String,
        limitMinutes: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            usersCol.document(uid)
                .collection("children").document(childId)
                .update("dailyLimitMinutes", limitMinutes)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCommunityPosts(): Result<List<CommunityPostEntity>> = withContext(Dispatchers.IO) {
        try {
            val snap = communityCol
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            val posts = snap.documents.mapNotNull { doc ->
                doc.toObject(CommunityPostEntity::class.java)?.copy(id = doc.id)
            }
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCommunityPost(post: CommunityPostEntity): Result<CommunityPostEntity> = withContext(Dispatchers.IO) {
        try {
            val postId = post.id.ifBlank { UUID.randomUUID().toString() }
            val finalPost = post.copy(id = postId, createdAt = System.currentTimeMillis())
            communityCol.document(postId).set(finalPost).await()
            Result.success(finalPost)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun togglePostLike(postId: String, uid: String, currentlyLiked: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val ref = communityCol.document(postId)
            if (currentlyLiked) {
                ref.update(
                    mapOf(
                        "likedBy" to FieldValue.arrayRemove(uid),
                        "likesCount" to FieldValue.increment(-1L)
                    )
                ).await()
            } else {
                ref.update(
                    mapOf(
                        "likedBy" to FieldValue.arrayUnion(uid),
                        "likesCount" to FieldValue.increment(1L)
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getComments(postId: String): Result<List<PostCommentEntity>> = withContext(Dispatchers.IO) {
        try {
            val snap = communityCol.document(postId)
                .collection("comments")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .await()
            val comments = snap.documents.mapNotNull { doc ->
                doc.toObject(PostCommentEntity::class.java)?.copy(id = doc.id)
            }
            Result.success(comments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addComment(postId: String, comment: PostCommentEntity): Result<PostCommentEntity> = withContext(Dispatchers.IO) {
        try {
            val commentId = comment.id.ifBlank { UUID.randomUUID().toString() }
            val finalComment = comment.copy(id = commentId, createdAt = System.currentTimeMillis())
            val postRef = communityCol.document(postId)
            postRef.collection("comments").document(commentId).set(finalComment).await()
            postRef.update("commentsCount", FieldValue.increment(1L)).await()
            Result.success(finalComment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
