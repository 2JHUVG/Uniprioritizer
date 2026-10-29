package com.example.uniprioritizer.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val TODAY = "today"
    const val WEEK = "week"
    const val COURSES = "courses"
    const val SETTINGS = "settings"
    const val NEW_TASK = "new_task"
    const val COURSE_ID_ARG = "courseId"
    const val TASK_ID_ARG = "taskId"
    const val TASK_DETAIL = "task/{$COURSE_ID_ARG}/{$TASK_ID_ARG}"
    const val PENDING_SYNC_ARG = "pendingSync"
    const val TASK_COMPLETED = "task_completed/{$PENDING_SYNC_ARG}"

    fun taskDetail(courseId: String, taskId: String) = "task/$courseId/$taskId"

    fun taskCompleted(pendingSync: Boolean) = "task_completed/$pendingSync"
}

enum class TopLevelDestination(val route: String, val label: String, val symbol: String) {
    TODAY(Routes.TODAY, "Hoy", "▣"),
    WEEK(Routes.WEEK, "Semana", "▥"),
    COURSES(Routes.COURSES, "Cursos", "◆"),
    SETTINGS(Routes.SETTINGS, "Ajustes", "⚙")
}
