package com.example.uniprioritizer.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

const val OVERLOAD_HOURS = 6.0
const val OVERLOAD_TASK_COUNT = 3
private const val URGENCY_WINDOW_DAYS = 7
private const val IMPORTANCE_MULTIPLIER = 10

enum class Importance(val weight: Int, val label: String) {
    HIGH(3, "Alta"),
    MEDIUM(2, "Media"),
    LOW(1, "Baja")
}

enum class TaskType(val label: String) {
    HOMEWORK("Tarea"),
    PROJECT("Proyecto"),
    EXAM("Examen")
}

data class Task(
    val id: String,
    val courseId: String,
    val courseName: String,
    val name: String,
    val type: TaskType,
    val dueDate: LocalDateTime,
    val importance: Importance,
    val estimatedHours: Double,
    val completed: Boolean = false
)

data class NewTask(
    val courseId: String,
    val name: String,
    val type: TaskType,
    val dueDate: LocalDateTime,
    val importance: Importance,
    val estimatedHours: Double
)

fun Task.priorityScore(today: LocalDate = LocalDate.now()): Int {
    val daysLeft = ChronoUnit.DAYS.between(today, dueDate.toLocalDate()).toInt()
    return importance.weight * IMPORTANCE_MULTIPLIER + maxOf(0, URGENCY_WINDOW_DAYS - daysLeft)
}

fun List<Task>.pendingByPriority(today: LocalDate = LocalDate.now()): List<Task> =
    filter { !it.completed }
        .sortedWith(compareByDescending<Task> { it.priorityScore(today) }.thenBy { it.dueDate })

fun List<Task>.pendingOn(day: LocalDate): List<Task> =
    filter { !it.completed && it.dueDate.toLocalDate() == day }

fun List<Task>.isOverloaded(day: LocalDate): Boolean {
    val dayTasks = pendingOn(day)
    return dayTasks.sumOf { it.estimatedHours } >= OVERLOAD_HOURS || dayTasks.size >= OVERLOAD_TASK_COUNT
}
