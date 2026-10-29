package com.example.uniprioritizer.data.remote

import com.example.uniprioritizer.domain.model.DataException
import com.example.uniprioritizer.domain.model.WriteStatus
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

/** Time a write waits for the server before reporting it as queued for later sync. */
private const val WRITE_CONFIRMATION_TIMEOUT_MS = 4_000L

/** Time a request that needs the server (sign in, register) waits before failing. */
const val SERVER_REQUEST_TIMEOUT_MS = 10_000L

fun Query.snapshotFlow(): Flow<QuerySnapshot> = callbackFlow {
    val registration = addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
        if (error != null) {
            close(error)
        } else if (snapshot != null) {
            trySend(snapshot)
        }
    }
    awaitClose { registration.remove() }
}.catch { throw it.toDataException() }

fun DocumentReference.snapshotFlow(): Flow<DocumentSnapshot> = callbackFlow {
    val registration = addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
        if (error != null) {
            close(error)
        } else if (snapshot != null) {
            trySend(snapshot)
        }
    }
    awaitClose { registration.remove() }
}.catch { throw it.toDataException() }

/**
 * Firestore applies writes to its local cache right away, but `await()` only returns once the
 * server confirms. Offline that never happens, so after a short wait the write is reported as
 * pending and Firestore sends it when the connection returns.
 */
suspend fun Task<Void>.awaitWrite(): WriteStatus {
    val confirmed = try {
        withTimeoutOrNull(WRITE_CONFIRMATION_TIMEOUT_MS) {
            await()
            true
        }
    } catch (error: FirebaseFirestoreException) {
        throw error.toDataException()
    }
    return if (confirmed == true) WriteStatus.SYNCED else WriteStatus.PENDING_SYNC
}

suspend fun <T> Task<T>.awaitServer(): T = try {
    withTimeout(SERVER_REQUEST_TIMEOUT_MS) { await() }
} catch (error: TimeoutCancellationException) {
    throw DataException("La conexión tardó demasiado. Revisa tu internet e inténtalo de nuevo.", error)
} catch (error: CancellationException) {
    throw error
} catch (error: Exception) {
    throw error.toDataException()
}

fun Throwable.toDataException(): DataException = when (this) {
    is DataException -> this
    is FirebaseNetworkException ->
        DataException("Sin conexión a internet. Revisa tu red e inténtalo de nuevo.", this)
    is FirebaseAuthWeakPasswordException ->
        DataException("La contraseña es muy débil. Usa al menos 6 caracteres.", this)
    is FirebaseAuthInvalidUserException,
    is FirebaseAuthInvalidCredentialsException ->
        DataException("Correo o contraseña incorrectos.", this)
    is FirebaseAuthUserCollisionException ->
        DataException("Ya existe una cuenta con ese correo.", this)
    is FirebaseFirestoreException -> when (code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            DataException("No tienes permiso para acceder a estos datos.", this)
        FirebaseFirestoreException.Code.UNAVAILABLE ->
            DataException("No hay conexión con el servidor. Inténtalo más tarde.", this)
        else -> reportUnexpected(this)
    }
    else -> reportUnexpected(this)
}

private fun reportUnexpected(error: Throwable): DataException {
    FirebaseCrashlytics.getInstance().recordException(error)
    return DataException("Ocurrió un error inesperado. Inténtalo de nuevo.", error)
}
