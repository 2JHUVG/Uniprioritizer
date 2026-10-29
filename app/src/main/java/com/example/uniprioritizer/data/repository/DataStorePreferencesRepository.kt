package com.example.uniprioritizer.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.uniprioritizer.domain.model.ThemeMode
import com.example.uniprioritizer.domain.model.UserPreferences
import com.example.uniprioritizer.domain.model.WeekStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStorePreferencesRepository(
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    override fun observePreferences(): Flow<UserPreferences> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { preferences ->
            UserPreferences(
                themeMode = ThemeMode.entries.firstOrNull { it.name == preferences[THEME_MODE] }
                    ?: ThemeMode.SYSTEM,
                weekStart = WeekStart.entries.firstOrNull { it.name == preferences[WEEK_START] }
                    ?: WeekStart.MONDAY
            )
        }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { it[THEME_MODE] = themeMode.name }
    }

    override suspend fun setWeekStart(weekStart: WeekStart) {
        dataStore.edit { it[WEEK_START] = weekStart.name }
    }

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val WEEK_START = stringPreferencesKey("week_start")
    }
}
