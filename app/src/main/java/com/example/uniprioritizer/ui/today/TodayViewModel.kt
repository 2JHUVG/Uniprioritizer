package com.example.uniprioritizer.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniprioritizer.data.repository.AuthRepository
import com.example.uniprioritizer.data.repository.DraftRepository
import com.example.uniprioritizer.data.repository.TaskRepository
import com.example.uniprioritizer.domain.model.Task
import com.example.uniprioritizer.domain.model.pendingByPriority
import com.example.uniprioritizer.ui.common.LOAD_TIMEOUT_MESSAGE
import com.example.uniprioritizer.ui.common.LOAD_TIMEOUT_MS
import com.example.uniprioritizer.ui.common.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TodayUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val userName: String = "",
    val pendingTasks: List<Task> = emptyList(),
    val draftName: String? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && pendingTasks.isEmpty()
}

class TodayViewModel(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository,
    private val draftRepository: DraftRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadTasks()
        viewModelScope.launch {
            draftRepository.observeDraft().collect { draft ->
                _uiState.update { it.copy(draftName = draft?.name?.ifBlank { "Sin nombre" }) }
            }
        }
    }

    fun onRetry() = loadTasks()

    fun onDiscardDraft() {
        viewModelScope.launch { draftRepository.clearDraft() }
    }

    private fun loadTasks() {
        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        loadJob = viewModelScope.launch {
            val timeoutJob = launch {
                delay(LOAD_TIMEOUT_MS)
                _uiState.update {
                    if (it.isLoading) it.copy(isLoading = false, errorMessage = LOAD_TIMEOUT_MESSAGE) else it
                }
            }
            combine(taskRepository.observeTasks(), authRepository.observeUser()) { tasks, user ->
                tasks to user
            }
                .catch { error ->
                    timeoutJob.cancel()
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
                }
                .collect { (tasks, user) ->
                    timeoutJob.cancel()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            isOffline = tasks.fromCache,
                            userName = user?.name.orEmpty(),
                            pendingTasks = tasks.data.pendingByPriority()
                        )
                    }
                }
        }
    }
}
