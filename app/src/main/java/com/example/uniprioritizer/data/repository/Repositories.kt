package com.example.uniprioritizer.data.repository

import com.example.uniprioritizer.domain.model.Course
import com.example.uniprioritizer.domain.model.NewTask
import com.example.uniprioritizer.domain.model.SyncedData
import com.example.uniprioritizer.domain.model.Task
import com.example.uniprioritizer.domain.model.TaskDraft
import com.example.uniprioritizer.domain.model.ThemeMode
import com.example.uniprioritizer.domain.model.User
import com.example.uniprioritizer.domain.model.UserPreferences
import com.example.uniprioritizer.domain.model.WeekStart
import com.example.uniprioritizer.domain.model.WriteStatus
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isSignedIn: Boolean
    fun observeSignedIn(): Flow<Boolean>
    fun observeUser(): Flow<User?>
    suspend fun signIn(email: String, password: String)
    suspend fun register(name: String, email: String, password: String)
    fun signOut()
}

interface CourseRepository {
    fun observeCourses(): Flow<SyncedData<List<Course>>>
    suspend fun addCourse(name: String, code: String): WriteStatus
    suspend fun deleteCourse(courseId: String): WriteStatus
}

interface TaskRepository {
    fun observeTasks(): Flow<SyncedData<List<Task>>>
    fun observeTask(courseId: String, taskId: String): Flow<SyncedData<Task?>>
    suspend fun addTask(newTask: NewTask): WriteStatus
    suspend fun setCompleted(courseId: String, taskId: String, completed: Boolean): WriteStatus
    suspend fun deleteTask(courseId: String, taskId: String): WriteStatus
}

interface DraftRepository {
    fun observeDraft(): Flow<TaskDraft?>
    suspend fun saveDraft(draft: TaskDraft)
    suspend fun clearDraft()
}

interface PreferencesRepository {
    fun observePreferences(): Flow<UserPreferences>
    suspend fun setThemeMode(themeMode: ThemeMode)
    suspend fun setWeekStart(weekStart: WeekStart)
}
