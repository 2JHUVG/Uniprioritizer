package com.example.uniprioritizer.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniprioritizer.data.repository.AuthRepository
import com.example.uniprioritizer.data.repository.DraftRepository
import com.example.uniprioritizer.data.repository.PreferencesRepository
import com.example.uniprioritizer.domain.model.ThemeMode
import com.example.uniprioritizer.domain.model.WeekStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userName: String = "",
    val userEmail: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val weekStart: WeekStart = WeekStart.MONDAY,
    val isSignedOut: Boolean = false
)

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val authRepository: AuthRepository,
    private val draftRepository: DraftRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.observePreferences().collect { preferences ->
                _uiState.update {
                    it.copy(themeMode = preferences.themeMode, weekStart = preferences.weekStart)
                }
            }
        }
        viewModelScope.launch {
            authRepository.observeUser()
                .catch { emit(null) }
                .collect { user ->
                    _uiState.update {
                        it.copy(userName = user?.name.orEmpty(), userEmail = user?.email.orEmpty())
                    }
                }
        }
    }

    fun onThemeModeSelect(themeMode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(themeMode) }
    }

    fun onWeekStartSelect(weekStart: WeekStart) {
        viewModelScope.launch { preferencesRepository.setWeekStart(weekStart) }
    }

    fun onSignOut() {
        viewModelScope.launch {
            draftRepository.clearDraft()
            authRepository.signOut()
            _uiState.update { it.copy(isSignedOut = true) }
        }
    }
}
