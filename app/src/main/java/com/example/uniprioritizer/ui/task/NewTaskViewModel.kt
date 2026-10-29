package com.example.uniprioritizer.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniprioritizer.data.repository.CourseRepository
import com.example.uniprioritizer.data.repository.DraftRepository
import com.example.uniprioritizer.data.repository.TaskRepository
import com.example.uniprioritizer.domain.model.Course
import com.example.uniprioritizer.domain.model.Importance
import com.example.uniprioritizer.domain.model.NewTask
import com.example.uniprioritizer.domain.model.TaskDraft
import com.example.uniprioritizer.domain.model.TaskType
import com.example.uniprioritizer.domain.model.WriteStatus
import com.example.uniprioritizer.ui.common.DRAFT_SAVE_DELAY_MS
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

const val DUE_DATE_PATTERN = "dd/MM/uuuu HH:mm"
private const val MAX_ESTIMATED_HOURS = 100.0

private val dueDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern(DUE_DATE_PATTERN).withResolverStyle(ResolverStyle.STRICT)

private fun parseDueDate(value: String): LocalDateTime? =
    runCatching { LocalDateTime.parse(value.trim(), dueDateFormatter) }.getOrNull()

private fun parseHours(value: String): Double? = value.trim().replace(',', '.').toDoubleOrNull()

data class NewTaskUiState(
    val name: String = "",
    val courseId: String = "",
    val dueDate: String = "",
    val estimatedHours: String = "",
    val type: TaskType = TaskType.HOMEWORK,
    val importance: Importance = Importance.MEDIUM,
    val courses: List<Course> = emptyList(),
    val isLoadingCourses: Boolean = true,
    val coursesError: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val savedStatus: WriteStatus? = null
) {
    val nameError: String?
        get() = if (name.isEmpty()) null else FormValidation.nameError(name, "el nombre")

    val dueDateError: String?
        get() {
            if (dueDate.isBlank()) return null
            val parsed = parseDueDate(dueDate) ?: return "Usa el formato 30/10/2026 18:30"
            return if (parsed.isBefore(LocalDateTime.now())) "La fecha ya pasó" else null
        }

    val estimatedHoursError: String?
        get() {
            if (estimatedHours.isBlank()) return null
            val hours = parseHours(estimatedHours)
            return when {
                hours == null -> "Ingresa un número, ej. 1.5"
                hours <= 0.0 -> "Debe ser mayor que cero"
                hours > MAX_ESTIMATED_HOURS -> "Máximo ${MAX_ESTIMATED_HOURS.toInt()} horas"
                else -> null
            }
        }

    val selectedCourse: Course?
        get() = courses.firstOrNull { it.id == courseId }

    val canSave: Boolean
        get() = !isSaving && name.isNotBlank() && nameError == null &&
            selectedCourse != null &&
            dueDate.isNotBlank() && dueDateError == null &&
            estimatedHours.isNotBlank() && estimatedHoursError == null

    fun toDraft(): TaskDraft = TaskDraft(
        name = name,
        courseId = courseId,
        dueDate = dueDate,
        estimatedHours = estimatedHours,
        type = type,
        importance = importance
    )
}

class NewTaskViewModel(
    private val taskRepository: TaskRepository,
    private val courseRepository: CourseRepository,
    private val draftRepository: DraftRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewTaskUiState())
    val uiState: StateFlow<NewTaskUiState> = _uiState.asStateFlow()

    private var draftJob: Job? = null
    private var coursesJob: Job? = null

    init {
        viewModelScope.launch {
            draftRepository.observeDraft().first()?.let { draft ->
                _uiState.update {
                    it.copy(
                        name = draft.name,
                        courseId = draft.courseId,
                        dueDate = draft.dueDate,
                        estimatedHours = draft.estimatedHours,
                        type = draft.type,
                        importance = draft.importance
                    )
                }
            }
        }
        loadCourses()
    }

    fun onRetry() = loadCourses()

    fun onNameChange(name: String) = updateForm { it.copy(name = name) }

    fun onCourseSelect(courseId: String) = updateForm { it.copy(courseId = courseId) }

    fun onDueDateChange(dueDate: String) = updateForm { it.copy(dueDate = dueDate) }

    fun onEstimatedHoursChange(hours: String) = updateForm { it.copy(estimatedHours = hours) }

    fun onTypeSelect(type: TaskType) = updateForm { it.copy(type = type) }

    fun onImportanceSelect(importance: Importance) = updateForm { it.copy(importance = importance) }

    fun onSave() {
        val state = _uiState.value
        if (!state.canSave) return
        val dueDate = parseDueDate(state.dueDate) ?: return
        val hours = parseHours(state.estimatedHours) ?: return
        _uiState.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            try {
                val status = taskRepository.addTask(
                    NewTask(
                        courseId = state.courseId,
                        name = state.name.trim(),
                        type = state.type,
                        dueDate = dueDate,
                        importance = state.importance,
                        estimatedHours = hours
                    )
                )
                draftJob?.cancel()
                draftRepository.clearDraft()
                _uiState.update { it.copy(isSaving = false, savedStatus = status) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = error.toUserMessage()) }
            }
        }
    }

    private fun updateForm(transform: (NewTaskUiState) -> NewTaskUiState) {
        _uiState.update { transform(it).copy(saveError = null) }
        draftJob?.cancel()
        draftJob = viewModelScope.launch {
            delay(DRAFT_SAVE_DELAY_MS)
            draftRepository.saveDraft(_uiState.value.toDraft())
        }
    }

    private fun loadCourses() {
        coursesJob?.cancel()
        _uiState.update { it.copy(isLoadingCourses = true, coursesError = null) }
        coursesJob = viewModelScope.launch {
            val timeoutJob = launch {
                delay(LOAD_TIMEOUT_MS)
                _uiState.update {
                    if (it.isLoadingCourses) {
                        it.copy(isLoadingCourses = false, coursesError = LOAD_TIMEOUT_MESSAGE)
                    } else {
                        it
                    }
                }
            }
            courseRepository.observeCourses()
                .catch { error ->
                    timeoutJob.cancel()
                    _uiState.update {
                        it.copy(isLoadingCourses = false, coursesError = error.toUserMessage())
                    }
                }
                .collect { courses ->
                    timeoutJob.cancel()
                    _uiState.update {
                        it.copy(isLoadingCourses = false, coursesError = null, courses = courses.data)
                    }
                }
        }
    }
}
