package com.example.uniprioritizer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.uniprioritizer.domain.model.Importance
import com.example.uniprioritizer.domain.model.TaskDraft
import com.example.uniprioritizer.domain.model.TaskType

/** The app keeps a single draft of the "new task" form, so the row id is fixed. */
const val CURRENT_DRAFT_ID = 1

@Entity(tableName = "task_drafts")
data class TaskDraftEntity(
    @PrimaryKey val id: Int = CURRENT_DRAFT_ID,
    val name: String,
    val courseId: String,
    val dueDate: String,
    val estimatedHours: String,
    val type: String,
    val importance: String,
    val updatedAt: Long
)

fun TaskDraftEntity.toDomain(): TaskDraft = TaskDraft(
    name = name,
    courseId = courseId,
    dueDate = dueDate,
    estimatedHours = estimatedHours,
    type = TaskType.entries.firstOrNull { it.name == type } ?: TaskType.HOMEWORK,
    importance = Importance.entries.firstOrNull { it.name == importance } ?: Importance.MEDIUM
)

fun TaskDraft.toEntity(): TaskDraftEntity = TaskDraftEntity(
    name = name,
    courseId = courseId,
    dueDate = dueDate,
    estimatedHours = estimatedHours,
    type = type.name,
    importance = importance.name,
    updatedAt = System.currentTimeMillis()
)
