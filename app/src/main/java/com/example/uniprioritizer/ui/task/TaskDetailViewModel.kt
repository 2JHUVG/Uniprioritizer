package com.example.uniprioritizer.ui.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniprioritizer.data.repository.TaskRepository
import com.example.uniprioritizer.domain.model.Task
import com.example.uniprioritizer.domain.model.WriteStatus
import com.example.uniprioritizer.navigation.Routes
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskDetailUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val task: Task? = null,
    val isUpdating: Boolean = false,
    val actionError: String? = null,
    val showDeleteConfirmation: Boolean = false,
    val completedStatus: WriteStatus? = null,
    val isDeleted: Boolean = false
) {
    val isNotFound: Boolean
        get() = !isLoading && errorMessage == null && task == null
}

class TaskDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val courseId: String = savedStateHandle[Routes.COURSE_ID_ARG] ?: ""
    private val taskId: String = savedStateHandle[Routes.TASK_ID_ARG] ?: ""

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadTask()
    }

    fun onRetry() = loadTask()

    fun onToggleCompleted() {
        val task = _uiState.value.task ?: return
        val completed = !task.completed
        runAction {
            val status = taskRepository.setCompleted(courseId, taskId, completed)
            if (completed) {
                _uiState.update { it.copy(completedStatus = status) }
            }
        }
    }

    fun onDeleteClick() = _uiState.update { it.copy(showDeleteConfirmation = true) }

    fun onDeleteDismiss() = _uiState.update { it.copy(showDeleteConfirmation = false) }

    fun onDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
        runAction {
            loadJob?.cancel()
            taskRepository.deleteTask(courseId, taskId)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }

    fun onNavigationHandled() = _uiState.update { it.copy(completedStatus = null, isDeleted = false) }

    private fun runAction(action: suspend () -> Unit) {
        _uiState.update { it.copy(isUpdating = true, actionError = null) }
        viewModelScope.launch {
            try {
                action()
                _uiState.update { it.copy(isUpdating = false) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(isUpdating = false, actionError = error.toUserMessage()) }
            }
        }
    }

    private fun loadTask() {
        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        loadJob = viewModelScope.launch {
            val timeoutJob = launch {
                delay(LOAD_TIMEOUT_MS)
                _uiState.update {
                    if (it.isLoading) it.copy(isLoading = false, errorMessage = LOAD_TIMEOUT_MESSAGE) else it
                }
            }
            taskRepository.observeTask(courseId, taskId)
                .catch { error ->
                    timeoutJob.cancel()
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
                }
                .collect { result ->
                    timeoutJob.cancel()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            isOffline = result.fromCache,
                            task = result.data
                        )
                    }
                }
        }
    }
}
