package com.example.uniprioritizer.data.repository

import com.example.uniprioritizer.data.remote.CourseFields
import com.example.uniprioritizer.data.remote.FirestorePaths
import com.example.uniprioritizer.data.remote.awaitWrite
import com.example.uniprioritizer.data.remote.snapshotFlow
import com.example.uniprioritizer.data.remote.toCourse
import com.example.uniprioritizer.data.remote.toDataException
import com.example.uniprioritizer.domain.model.Course
import com.example.uniprioritizer.domain.model.SyncedData
import com.example.uniprioritizer.domain.model.WriteStatus
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

private const val CACHE_READ_TIMEOUT_MS = 4_000L

class FirestoreCourseRepository(
    private val firestore: FirebaseFirestore,
    private val paths: FirestorePaths
) : CourseRepository {

    override fun observeCourses(): Flow<SyncedData<List<Course>>> =
        paths.courses().orderBy(CourseFields.NAME).snapshotFlow().map { snapshot ->
            SyncedData(
                data = snapshot.documents.mapNotNull { it.toCourse() },
                fromCache = snapshot.metadata.isFromCache
            )
        }

    override suspend fun addCourse(name: String, code: String): WriteStatus =
        paths.courses().document().set(
            mapOf(
                CourseFields.NAME to name.trim(),
                CourseFields.CODE to code.trim().uppercase(),
                CourseFields.CREATED_AT to FieldValue.serverTimestamp()
            )
        ).awaitWrite()

    override suspend fun deleteCourse(courseId: String): WriteStatus {
        // Firestore does not delete subcollections, so the course tasks go in the same batch.
        val tasks = try {
            withTimeoutOrNull(CACHE_READ_TIMEOUT_MS) { paths.tasks(courseId).get().await() }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw error.toDataException()
        }
        val batch = firestore.batch()
        tasks?.documents?.forEach { batch.delete(it.reference) }
        batch.delete(paths.course(courseId))
        return batch.commit().awaitWrite()
    }
}
