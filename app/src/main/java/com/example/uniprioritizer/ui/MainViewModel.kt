package com.example.uniprioritizer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniprioritizer.data.repository.AuthRepository
import com.example.uniprioritizer.data.repository.PreferencesRepository
import com.example.uniprioritizer.domain.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isSignedIn: Boolean = false
)

class MainViewModel(
    preferencesRepository: PreferencesRepository,
    authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState(isSignedIn = authRepository.isSignedIn))
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.observePreferences().collect { preferences ->
                _uiState.update { it.copy(themeMode = preferences.themeMode) }
            }
        }
    }
}
