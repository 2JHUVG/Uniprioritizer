package com.example.uniprioritizer.ui.common

import com.example.uniprioritizer.domain.model.DataException
import com.example.uniprioritizer.domain.model.WriteStatus

/** A screen that has not received data after this time shows an error instead of loading forever. */
const val LOAD_TIMEOUT_MS = 10_000L

/** Delay before the new task form is copied to the Room draft, so not every keystroke writes. */
const val DRAFT_SAVE_DELAY_MS = 400L

const val LOAD_TIMEOUT_MESSAGE =
    "No pudimos cargar tus datos. Revisa tu conexión e inténtalo de nuevo."

const val PENDING_SYNC_MESSAGE =
    "Sin conexión: el cambio se guardó en el teléfono y se enviará al recuperar la conexión."

fun Throwable.toUserMessage(): String =
    (this as? DataException)?.message ?: "Ocurrió un error inesperado. Inténtalo de nuevo."

fun WriteStatus.toUserMessage(syncedMessage: String): String = when (this) {
    WriteStatus.SYNCED -> syncedMessage
    WriteStatus.PENDING_SYNC -> PENDING_SYNC_MESSAGE
}
