package com.example.uniprioritizer.data.remote

import com.example.uniprioritizer.domain.model.Course
import com.example.uniprioritizer.domain.model.Importance
import com.example.uniprioritizer.domain.model.NewTask
import com.example.uniprioritizer.domain.model.Task
import com.example.uniprioritizer.domain.model.TaskType
import com.example.uniprioritizer.domain.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

object UserFields {
    const val NAME = "name"
    const val EMAIL = "email"
    const val CREATED_AT = "createdAt"
}

object CourseFields {
    const val NAME = "name"
    const val CODE = "code"
    const val CREATED_AT = "createdAt"
}

object TaskFields {
    const val NAME = "name"
    const val TYPE = "type"
    const val DUE_DATE = "dueDate"
    const val IMPORTANCE = "importance"
    const val ESTIMATED_HOURS = "estimatedHours"
    const val COMPLETED = "completed"
    const val CREATED_AT = "createdAt"
}

fun DocumentSnapshot.toUser(): User? {
    if (!exists()) return null
    return User(
        uid = reference.id,
        name = getString(UserFields.NAME).orEmpty(),
        email = getString(UserFields.EMAIL).orEmpty()
    )
}

fun DocumentSnapshot.toCourse(): Course? {
    val name = getString(CourseFields.NAME) ?: return null
    return Course(id = id, name = name, code = getString(CourseFields.CODE).orEmpty())
}

fun DocumentSnapshot.toTask(course: Course): Task? {
    val name = getString(TaskFields.NAME) ?: return null
    val dueDate = getTimestamp(TaskFields.DUE_DATE) ?: return null
    return Task(
        id = id,
        courseId = course.id,
        courseName = course.name,
        name = name,
        type = enumOrDefault(getString(TaskFields.TYPE), TaskType.HOMEWORK),
        dueDate = dueDate.toLocalDateTime(),
        importance = enumOrDefault(getString(TaskFields.IMPORTANCE), Importance.MEDIUM),
        estimatedHours = getDouble(TaskFields.ESTIMATED_HOURS) ?: 0.0,
        completed = getBoolean(TaskFields.COMPLETED) ?: false
    )
}

fun NewTask.toFirestoreMap(): Map<String, Any> = mapOf(
    TaskFields.NAME to name,
    TaskFields.TYPE to type.name,
    TaskFields.DUE_DATE to dueDate.toTimestamp(),
    TaskFields.IMPORTANCE to importance.name,
    TaskFields.ESTIMATED_HOURS to estimatedHours,
    TaskFields.COMPLETED to false,
    TaskFields.CREATED_AT to FieldValue.serverTimestamp()
)

private inline fun <reified T : Enum<T>> enumOrDefault(value: String?, default: T): T =
    enumValues<T>().firstOrNull { it.name == value } ?: default

private fun Timestamp.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(toDate().toInstant(), ZoneId.systemDefault())

private fun LocalDateTime.toTimestamp(): Timestamp =
    Timestamp(Date.from(atZone(ZoneId.systemDefault()).toInstant()))
