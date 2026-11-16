package com.example.uniprioritizer.ui.week

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uniprioritizer.data.repository.PreferencesRepository
import com.example.uniprioritizer.data.repository.TaskRepository
import com.example.uniprioritizer.domain.model.Task
import com.example.uniprioritizer.domain.model.WeekStart
import com.example.uniprioritizer.domain.model.isOverloaded
import com.example.uniprioritizer.domain.model.pendingOn
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
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

private const val DAYS_IN_WEEK = 7L

data class WeekDayUi(
    val date: LocalDate,
    val taskCount: Int,
    val isOverloaded: Boolean
)

data class WeekUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val weekDays: List<WeekDayUi> = emptyList(),
    val selectedDay: LocalDate = LocalDate.now(),
    val dayTasks: List<Task> = emptyList(),
    val dayHours: Double = 0.0,
    val isDayOverloaded: Boolean = false
) {
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && dayTasks.isEmpty()
}

class WeekViewModel(
    private val taskRepository: TaskRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WeekUiState())
    val uiState: StateFlow<WeekUiState> = _uiState.asStateFlow()

    private val selectedDay = MutableStateFlow(LocalDate.now())
    private var loadJob: Job? = null

    init {
        loadWeek()
    }

    fun onRetry() = loadWeek()

    fun onDaySelect(day: LocalDate) {
        selectedDay.value = day
    }

    fun onPreviousWeek() {
        selectedDay.update { it.minusDays(DAYS_IN_WEEK) }
    }

    fun onNextWeek() {
        selectedDay.update { it.plusDays(DAYS_IN_WEEK) }
    }

    private fun loadWeek() {
        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        loadJob = viewModelScope.launch {
            val timeoutJob = launch {
                delay(LOAD_TIMEOUT_MS)
                _uiState.update {
                    if (it.isLoading) it.copy(isLoading = false, errorMessage = LOAD_TIMEOUT_MESSAGE) else it
                }
            }
            combine(
                taskRepository.observeTasks(),
                preferencesRepository.observePreferences(),
                selectedDay
            ) { tasks, preferences, day ->
                buildState(tasks.data, tasks.fromCache, preferences.weekStart, day)
            }
                .catch { error ->
                    timeoutJob.cancel()
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.toUserMessage()) }
                }
                .collect { state ->
                    timeoutJob.cancel()
                    _uiState.value = state
                }
        }
    }

    private fun buildState(
        tasks: List<Task>,
        fromCache: Boolean,
        weekStart: WeekStart,
        day: LocalDate
    ): WeekUiState {
        val firstDay = day.with(TemporalAdjusters.previousOrSame(weekStart.firstDay))
        val weekDays = (0 until DAYS_IN_WEEK).map { offset ->
            val date = firstDay.plusDays(offset)
            WeekDayUi(
                date = date,
                taskCount = tasks.pendingOn(date).size,
                isOverloaded = tasks.isOverloaded(date)
            )
        }
        val dayTasks = tasks.pendingOn(day).sortedBy { it.dueDate }
        return WeekUiState(
            isLoading = false,
            isOffline = fromCache,
            weekDays = weekDays,
            selectedDay = day,
            dayTasks = dayTasks,
            dayHours = dayTasks.sumOf { it.estimatedHours },
            isDayOverloaded = tasks.isOverloaded(day)
        )
    }
}
