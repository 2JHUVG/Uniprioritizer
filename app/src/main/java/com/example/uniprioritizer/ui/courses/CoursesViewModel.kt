package com.example.uniprioritizer.ui.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniprioritizer.data.repository.CourseRepository
import com.example.uniprioritizer.data.repository.TaskRepository
import com.example.uniprioritizer.domain.model.Course
import com.example.uniprioritizer.ui.common.FormValidation
import com.example.uniprioritizer.ui.common.LOAD_TIMEOUT_MESSAGE
import com.example.uniprioritizer.ui.common.LOAD_TIMEOUT_MS
import com.example.uniprioritizer.ui.common.toUserMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MAX_CODE_LENGTH = 10

data class CourseSummary(
    val course: Course,
    val pendingCount: Int
)

data class CourseForm(
    val name: String = "",
    val code: String = "",
    val nameError: String? = null,
    val codeError: String? = null,
    val isSaving: Boolean = false
) {
    val canSave: Boolean
        get() = !isSaving && FormValidation.nameError(name, "el nombre del curso") == null &&
            codeErrorFor(code) == null
}

private fun codeErrorFor(code: String): String? =
    if (code.trim().length > MAX_CODE_LENGTH) "Máximo $MAX_CODE_LENGTH caracteres" else null

data class CoursesUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val courses: List<CourseSummary> = emptyList(),
    val query: String = "",
    val courseForm: CourseForm? = null,
    val courseToDelete: Course? = null,
    val message: String? = null
) {
    val visibleCourses: List<CourseSummary>
        get() = if (query.isBlank()) {
            courses
        } else {
            courses.filter {
                it.course.name.contains(query.trim(), ignoreCase = true) ||
                    it.course.code.contains(query.trim(), ignoreCase = true)
            }
        }

    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && courses.isEmpty()
}

class CoursesViewModel(
    private val courseRepository: CourseRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CoursesUiState())
    val uiState: StateFlow<CoursesUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadCourses()
    }

    fun onRetry() = loadCourses()

    fun onQueryChange(query: String) = _uiState.update { it.copy(query = query) }

    fun onAddCourseClick() = _uiState.update { it.copy(courseForm = CourseForm()) }

    fun onCourseFormDismiss() = _uiState.update { it.copy(courseForm = null) }

    fun onCourseNameChange(name: String) = _uiState.update { state ->
        state.copy(
            courseForm = state.courseForm?.copy(
                name = name,
                nameError = FormValidation.nameError(name, "el nombre del curso")
            )
        )
    }

    fun onCourseCodeChange(code: String) = _uiState.update { state ->
        state.copy(courseForm = state.courseForm?.copy(code = code, codeError = codeErrorFor(code)))
    }

    fun onSaveCourse() {
        val form = _uiState.value.courseForm ?: return
        if (!form.canSave) return
        _uiState.update { it.copy(courseForm = form.copy(isSaving = true)) }
        viewModelScope.launch {
            try {
                val status = courseRepository.addCourse(form.name, form.code)
                _uiState.update {
                    it.copy(courseForm = null, message = status.toUserMessage("Curso agregado"))
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        courseForm = it.courseForm?.copy(isSaving = false),
                        message = error.toUserMessage()
                    )
                }
            }
        }
    }

    fun onDeleteCourseClick(course: Course) = _uiState.update { it.copy(courseToDelete = course) }

    fun onDeleteCourseDismiss() = _uiState.update { it.copy(courseToDelete = null) }

    fun onDeleteCourseConfirm() {
        val course = _uiState.value.courseToDelete ?: return
        _uiState.update { it.copy(courseToDelete = null) }
        viewModelScope.launch {
            try {
                val status = courseRepository.deleteCourse(course.id)
                _uiState.update { it.copy(message = status.toUserMessage("Curso eliminado")) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(message = error.toUserMessage()) }
            }
        }
    }

    fun onMessageShown() = _uiState.update { it.copy(message = null) }

    private fun loadCourses() {
        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        loadJob = viewModelScope.launch {
            val timeoutJob = launch {
                delay(LOAD_TIMEOUT_MS)
                _uiState.update {
                    if (it.isLoading) it.copy(isLoading = false, errorMessage = LOAD_TIMEOUT_MESSAGE) else it
                }
            }
            combine(courseRepository.observeCourses(), taskRepository.observeTasks()) { courses, tasks ->
                val pendingByCourse = tasks.data.filter { !it.completed }.groupingBy { it.courseId }.eachCount()
                val summaries = courses.data.map { CourseSummary(it, pendingByCourse[it.id] ?: 0) }
                summaries to (courses.fromCache || tasks.fromCache)
            }
                .catch { error ->
                    timeoutJob.cancel()
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
                }
                .collect { (summaries, fromCache) ->
                    timeoutJob.cancel()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            isOffline = fromCache,
                            courses = summaries
                        )
                    }
                }
        }
    }
}
