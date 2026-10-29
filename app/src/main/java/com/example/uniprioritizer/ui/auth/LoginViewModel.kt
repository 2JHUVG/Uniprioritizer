package com.example.uniprioritizer.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniprioritizer.data.repository.AuthRepository
import com.example.uniprioritizer.ui.common.FormValidation
import com.example.uniprioritizer.ui.common.toUserMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSignedIn: Boolean = false
) {
    val canSubmit: Boolean
        get() = !isLoading && FormValidation.emailError(email) == null &&
            FormValidation.passwordError(password) == null
}

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) = _uiState.update {
        it.copy(email = email, emailError = FormValidation.emailError(email), errorMessage = null)
    }

    fun onPasswordChange(password: String) = _uiState.update {
        it.copy(
            password = password,
            passwordError = FormValidation.passwordError(password),
            errorMessage = null
        )
    }

    fun onSubmit() {
        val state = _uiState.value
        if (!state.canSubmit) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                authRepository.signIn(state.email, state.password)
                _uiState.update { it.copy(isLoading = false, isSignedIn = true) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
            }
        }
    }
}
