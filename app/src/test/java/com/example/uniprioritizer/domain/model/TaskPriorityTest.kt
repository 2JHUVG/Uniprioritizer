package com.example.uniprioritizer.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class TaskPriorityTest {
    private val today = LocalDate.of(2026, 9, 21)

    private fun task(
        id: String,
        daysFromToday: Long,
        importance: Importance = Importance.MEDIUM,
        hours: Double = 1.0,
        completed: Boolean = false
    ) = Task(
        id = id,
        courseId = "course",
        courseName = "Física",
        name = "Actividad $id",
        type = TaskType.HOMEWORK,
        dueDate = today.plusDays(daysFromToday).atTime(18, 0),
        importance = importance,
        estimatedHours = hours,
        completed = completed
    )

    @Test
    fun priorityScore_combinesImportanceAndUrgency() {
        assertEquals(35, task("a", daysFromToday = 2, importance = Importance.HIGH).priorityScore(today))
        assertEquals(10, task("b", daysFromToday = 10, importance = Importance.LOW).priorityScore(today))
    }

    @Test
    fun pendingByPriority_skipsCompletedAndSortsByScore() {
        val tasks = listOf(
            task("low", daysFromToday = 5, importance = Importance.LOW),
            task("high", daysFromToday = 3, importance = Importance.HIGH),
            task("done", daysFromToday = 0, importance = Importance.HIGH, completed = true)
        )

        val ordered = tasks.pendingByPriority(today).map { it.id }

        assertEquals(listOf("high", "low"), ordered)
    }

    @Test
    fun isOverloaded_whenHoursOrTaskCountReachLimit() {
        val heavyDay = listOf(task("a", 1, hours = 4.0), task("b", 1, hours = 2.0))
        val busyDay = listOf(task("a", 1), task("b", 1), task("c", 1))
        val lightDay = listOf(task("a", 1, hours = 2.0))
        val day = today.plusDays(1)

        assertTrue(heavyDay.isOverloaded(day))
        assertTrue(busyDay.isOverloaded(day))
        assertFalse(lightDay.isOverloaded(day))
    }

    @Test
    fun pendingOn_onlyReturnsTasksDueThatDay() {
        val tasks = listOf(task("a", 1), task("b", 2))

        assertEquals(listOf("a"), tasks.pendingOn(LocalDateTime.of(2026, 9, 22, 0, 0).toLocalDate()).map { it.id })
    }
}
