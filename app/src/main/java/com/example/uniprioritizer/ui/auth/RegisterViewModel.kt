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

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistered: Boolean = false
) {
    val canSubmit: Boolean
        get() = !isLoading &&
            FormValidation.nameError(name) == null &&
            FormValidation.emailError(email) == null &&
            FormValidation.passwordError(password) == null &&
            confirmPasswordErrorFor(password, confirmPassword) == null
}

private fun confirmPasswordErrorFor(password: String, confirmPassword: String): String? = when {
    confirmPassword.isEmpty() -> "Repite la contraseña"
    confirmPassword != password -> "Las contraseñas no coinciden"
    else -> null
}

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) = _uiState.update {
        it.copy(name = name, nameError = FormValidation.nameError(name, "tu nombre"), errorMessage = null)
    }

    fun onEmailChange(email: String) = _uiState.update {
        it.copy(email = email, emailError = FormValidation.emailError(email), errorMessage = null)
    }

    fun onPasswordChange(password: String) = _uiState.update {
        it.copy(
            password = password,
            passwordError = FormValidation.passwordError(password),
            confirmPasswordError = if (it.confirmPassword.isEmpty()) {
                null
            } else {
                confirmPasswordErrorFor(password, it.confirmPassword)
            },
            errorMessage = null
        )
    }

    fun onConfirmPasswordChange(confirmPassword: String) = _uiState.update {
        it.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = confirmPasswordErrorFor(it.password, confirmPassword),
            errorMessage = null
        )
    }

    fun onSubmit() {
        val state = _uiState.value
        if (!state.canSubmit) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                authRepository.register(state.name, state.email, state.password)
                _uiState.update { it.copy(isLoading = false, isRegistered = true) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
            }
        }
    }
}
