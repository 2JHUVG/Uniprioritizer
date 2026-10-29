package com.example.uniprioritizer.data.remote

import com.example.uniprioritizer.domain.model.DataException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Firestore layout, every document belongs to the signed-in user:
 * users/{uid}
 * users/{uid}/courses/{courseId}
 * users/{uid}/courses/{courseId}/tasks/{taskId}
 */
class FirestorePaths(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    fun user(): DocumentReference = firestore.collection(USERS).document(requireUid())

    fun courses(): CollectionReference = user().collection(COURSES)

    fun course(courseId: String): DocumentReference = courses().document(courseId)

    fun tasks(courseId: String): CollectionReference = course(courseId).collection(TASKS)

    fun task(courseId: String, taskId: String): DocumentReference = tasks(courseId).document(taskId)

    private fun requireUid(): String =
        auth.currentUser?.uid ?: throw DataException("Tu sesión expiró. Vuelve a iniciar sesión.")

    companion object {
        const val USERS = "users"
        const val COURSES = "courses"
        const val TASKS = "tasks"
    }
}
