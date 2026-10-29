package com.example.uniprioritizer

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.uniprioritizer.data.local.UniPrioritizerDatabase
import com.example.uniprioritizer.data.remote.FirestorePaths
import com.example.uniprioritizer.data.repository.AuthRepository
import com.example.uniprioritizer.data.repository.CourseRepository
import com.example.uniprioritizer.data.repository.DataStorePreferencesRepository
import com.example.uniprioritizer.data.repository.DraftRepository
import com.example.uniprioritizer.data.repository.FirebaseAuthRepository
import com.example.uniprioritizer.data.repository.FirestoreCourseRepository
import com.example.uniprioritizer.data.repository.FirestoreTaskRepository
import com.example.uniprioritizer.data.repository.PreferencesRepository
import com.example.uniprioritizer.data.repository.RoomDraftRepository
import com.example.uniprioritizer.data.repository.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore

private val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

/** Manual dependency injection: builds every repository once and hands them to the ViewModels. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val paths = FirestorePaths(auth, firestore)
    private val database = UniPrioritizerDatabase.create(appContext)

    val authRepository: AuthRepository =
        FirebaseAuthRepository(auth, paths, FirebaseCrashlytics.getInstance())

    val courseRepository: CourseRepository = FirestoreCourseRepository(firestore, paths)

    val taskRepository: TaskRepository = FirestoreTaskRepository(paths, courseRepository)

    val draftRepository: DraftRepository = RoomDraftRepository(database.taskDraftDao())

    val preferencesRepository: PreferencesRepository =
        DataStorePreferencesRepository(appContext.preferencesDataStore)
}
