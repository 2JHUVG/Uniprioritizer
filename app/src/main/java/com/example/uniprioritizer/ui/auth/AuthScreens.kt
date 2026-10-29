package com.example.uniprioritizer.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.uniprioritizer.ui.components.ValidatedTextField

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuthScaffold(
        title = "UniPrioritizer",
        subtitle = "Inicia sesión para ver qué hacer primero.",
        modifier = modifier
    ) {
        ValidatedTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = "Correo",
            errorMessage = uiState.emailError,
            keyboardType = KeyboardType.Email
        )
        ValidatedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = "Contraseña",
            errorMessage = uiState.passwordError,
            keyboardType = KeyboardType.Password,
            isPassword = true
        )
        AuthError(uiState.errorMessage)
        SubmitButton(
            label = "Iniciar sesión",
            enabled = uiState.canSubmit,
            isLoading = uiState.isLoading,
            onClick = onSubmit
        )
        TextButton(onClick = onRegisterClick, modifier = Modifier.fillMaxWidth()) {
            Text("¿No tienes cuenta? Crear cuenta")
        }
    }
}

@Composable
fun RegisterScreen(
    uiState: RegisterUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuthScaffold(
        title = "Crear cuenta",
        subtitle = "Tus cursos y actividades se guardan en tu cuenta.",
        modifier = modifier
    ) {
        ValidatedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = "Nombre",
            errorMessage = uiState.nameError
        )
        ValidatedTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = "Correo",
            errorMessage = uiState.emailError,
            keyboardType = KeyboardType.Email
        )
        ValidatedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = "Contraseña",
            errorMessage = uiState.passwordError,
            supportingText = "Mínimo 6 caracteres",
            keyboardType = KeyboardType.Password,
            isPassword = true
        )
        ValidatedTextField(
            value = uiState.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "Confirmar contraseña",
            errorMessage = uiState.confirmPasswordError,
            keyboardType = KeyboardType.Password,
            isPassword = true
        )
        AuthError(uiState.errorMessage)
        SubmitButton(
            label = "Crear cuenta",
            enabled = uiState.canSubmit,
            isLoading = uiState.isLoading,
            onClick = onSubmit
        )
        TextButton(onClick = onLoginClick, modifier = Modifier.fillMaxWidth()) {
            Text("Ya tengo cuenta")
        }
    }
}

@Composable
private fun AuthScaffold(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.height(48.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = subtitle, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun AuthError(message: String?) {
    if (message != null) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SubmitButton(
    label: String,
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Text(label)
        }
    }
}
