package com.example.litutur.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

object FirestoreHelper {
    private val db = Firebase.firestore

    /**
     * Saves a new community post to the "posts" collection in Firestore.
     * @param caption Text caption of the post
     * @param imageUrl Optional Cloudinary URL of the uploaded photo
     * @param authorName Name of the user posting
     * @param authorTitle Title/role of user (e.g. "Bunda dari Budi (5 thn)")
     * @param milestoneTitle Optional milestone tag
     * @param bookTitle Optional book title reference
     */
    suspend fun createPost(
        caption: String,
        imageUrl: String? = null,
        authorName: String = "Orang Tua",
        authorTitle: String = "Anggota Komunitas Litutur",
        milestoneTitle: String? = null,
        bookTitle: String? = null
    ): String {
        val postMap = hashMapOf(
            "caption" to caption,
            "imageUrl" to (imageUrl ?: ""),
            "authorName" to authorName,
            "authorTitle" to authorTitle,
            "milestoneTitle" to (milestoneTitle ?: ""),
            "bookTitle" to (bookTitle ?: ""),
            "likesCount" to 0,
            "commentsCount" to 0,
            "createdAt" to FieldValue.serverTimestamp()
        )

        val docRef = db.collection("posts").add(postMap).await()
        return docRef.id
    }

    /**
     * Retrieves all posts from Firestore ordered by creation date descending.
     */
    suspend fun getPosts(): List<Map<String, Any>> {
        val snapshot = db.collection("posts")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { it.data?.plus("id" to it.id) }
    }
}
