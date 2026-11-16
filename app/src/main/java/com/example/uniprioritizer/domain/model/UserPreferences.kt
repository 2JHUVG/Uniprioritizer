package com.example.uniprioritizer.domain.model

import java.time.DayOfWeek

enum class ThemeMode(val label: String) {
    SYSTEM("Sistema"),
    LIGHT("Claro"),
    DARK("Oscuro")
}

enum class WeekStart(val firstDay: DayOfWeek, val label: String) {
    MONDAY(DayOfWeek.MONDAY, "Lunes"),
    SUNDAY(DayOfWeek.SUNDAY, "Domingo")
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val weekStart: WeekStart = WeekStart.MONDAY
)
