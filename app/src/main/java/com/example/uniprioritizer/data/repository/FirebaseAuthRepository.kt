package com.example.uniprioritizer.data.repository

import com.example.uniprioritizer.data.remote.FirestorePaths
import com.example.uniprioritizer.data.remote.UserFields
import com.example.uniprioritizer.data.remote.awaitServer
import com.example.uniprioritizer.data.remote.awaitWrite
import com.example.uniprioritizer.data.remote.snapshotFlow
import com.example.uniprioritizer.data.remote.toUser
import com.example.uniprioritizer.domain.model.DataException
import com.example.uniprioritizer.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val paths: FirestorePaths,
    private val crashlytics: FirebaseCrashlytics
) : AuthRepository {

    override val isSignedIn: Boolean
        get() = auth.currentUser != null

    override fun observeSignedIn(): Flow<Boolean> = observeUid().map { it != null }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeUser(): Flow<User?> = observeUid().flatMapLatest { uid ->
        if (uid == null) {
            flowOf(null)
        } else {
            paths.user().snapshotFlow().map { snapshot ->
                snapshot.toUser() ?: fallbackUser(uid)
            }
        }
    }

    override suspend fun signIn(email: String, password: String) {
        val result = auth.signInWithEmailAndPassword(email.trim(), password).awaitServer()
        result.user?.uid?.let { crashlytics.setUserId(it) }
    }

    override suspend fun register(name: String, email: String, password: String) {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).awaitServer()
        val firebaseUser = result.user ?: throw DataException("No se pudo crear la cuenta.")
        crashlytics.setUserId(firebaseUser.uid)
        val profile = UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()
        firebaseUser.updateProfile(profile).awaitWrite()
        paths.user().set(
            mapOf(
                UserFields.NAME to name.trim(),
                UserFields.EMAIL to email.trim(),
                UserFields.CREATED_AT to FieldValue.serverTimestamp()
            )
        ).awaitWrite()
    }

    override fun signOut() {
        auth.signOut()
        crashlytics.setUserId("")
    }

    private fun observeUid(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    private fun fallbackUser(uid: String): User = User(
        uid = uid,
        name = auth.currentUser?.displayName.orEmpty(),
        email = auth.currentUser?.email.orEmpty()
    )
}
