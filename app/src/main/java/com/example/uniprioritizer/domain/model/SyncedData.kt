package com.example.uniprioritizer.domain.model

/**
 * Data read from the remote source. [fromCache] is true when the device is offline and the
 * values come from the local Firestore cache, so the UI can warn the user.
 */
data class SyncedData<T>(
    val data: T,
    val fromCache: Boolean
)

/** Outcome of a write: confirmed by the server, or queued locally until the connection returns. */
enum class WriteStatus {
    SYNCED,
    PENDING_SYNC
}

data class TaskDraft(
    val name: String = "",
    val courseId: String = "",
    val dueDate: String = "",
    val estimatedHours: String = "",
    val type: TaskType = TaskType.HOMEWORK,
    val importance: Importance = Importance.MEDIUM
) {
    val isEmpty: Boolean
        get() = name.isBlank() && courseId.isBlank() && dueDate.isBlank() && estimatedHours.isBlank()
}
