package com.example.uniprioritizer.ui.common

import android.util.Patterns

const val MIN_PASSWORD_LENGTH = 6
const val MIN_NAME_LENGTH = 3

/** Each function returns the message to show next to the field, or null when the value is valid. */
object FormValidation {
    fun emailError(email: String): String? = when {
        email.isBlank() -> "Ingresa tu correo"
        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Correo no válido, ej. ana@uvg.edu.gt"
        else -> null
    }

    fun passwordError(password: String): String? = when {
        password.isEmpty() -> "Ingresa tu contraseña"
        password.length < MIN_PASSWORD_LENGTH -> "Mínimo $MIN_PASSWORD_LENGTH caracteres"
        else -> null
    }

    fun nameError(name: String, fieldLabel: String = "el nombre"): String? = when {
        name.isBlank() -> "Ingresa $fieldLabel"
        name.trim().length < MIN_NAME_LENGTH -> "Mínimo $MIN_NAME_LENGTH caracteres"
        else -> null
    }
}
