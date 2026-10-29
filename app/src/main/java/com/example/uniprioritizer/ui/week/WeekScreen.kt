package com.example.uniprioritizer.ui.week

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.uniprioritizer.domain.model.Task
import com.example.uniprioritizer.navigation.TopLevelDestination
import com.example.uniprioritizer.ui.components.AppBottomBar
import com.example.uniprioritizer.ui.components.EmptyContent
import com.example.uniprioritizer.ui.components.ErrorContent
import com.example.uniprioritizer.ui.components.LoadingContent
import com.example.uniprioritizer.ui.components.OfflineBanner
import com.example.uniprioritizer.ui.components.TaskRow
import com.example.uniprioritizer.ui.components.dayInitial
import com.example.uniprioritizer.ui.components.toHoursLabel
import com.example.uniprioritizer.ui.components.toLongLabel
import java.time.LocalDate

@Composable
fun WeekScreen(
    uiState: WeekUiState,
    onDaySelect: (LocalDate) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onTaskClick: (Task) -> Unit,
    onNewTaskClick: () -> Unit,
    onRetry: () -> Unit,
    onDestinationClick: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        bottomBar = { AppBottomBar(TopLevelDestination.WEEK, onDestinationClick) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onNewTaskClick) { Text("+ Nueva actividad") }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isOffline) OfflineBanner()
            when {
                uiState.isLoading -> LoadingContent(message = "Cargando tu semana…")
                uiState.errorMessage != null -> ErrorContent(uiState.errorMessage, onRetry)
                else -> WeekContent(uiState, onDaySelect, onPreviousWeek, onNextWeek, onTaskClick)
            }
        }
    }
}

@Composable
private fun WeekContent(
    uiState: WeekUiState,
    onDaySelect: (LocalDate) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onTaskClick: (Task) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onPreviousWeek) { Text("‹ Anterior") }
                Text("Tu semana", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onNextWeek) { Text("Siguiente ›") }
            }
        }
        item(key = "strip") { WeekStrip(uiState, onDaySelect) }
        item(key = "day_header") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(uiState.selectedDay.toLongLabel(), style = MaterialTheme.typography.titleMedium)
                if (uiState.isDayOverloaded) {
                    AssistChip(
                        onClick = {},
                        label = { Text("⚠ Día cargado: ${uiState.dayHours.toHoursLabel()} de trabajo") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            labelColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }
            }
        }
        if (uiState.isEmpty) {
            item(key = "empty") {
                EmptyContent(
                    title = "Día libre",
                    message = "No tienes entregas pendientes para este día."
                )
            }
        } else {
            items(items = uiState.dayTasks, key = { it.id }) { task ->
                TaskRow(task = task, onTaskClick = onTaskClick)
            }
        }
    }
}

@Composable
private fun WeekStrip(
    uiState: WeekUiState,
    onDaySelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        uiState.weekDays.forEach { day ->
            val isSelected = day.date == uiState.selectedDay
            val isToday = day.date == LocalDate.now()
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.clickable { onDaySelect(day.date) }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(day.date.dayInitial(), style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = day.date.dayOfMonth.toString(),
                        style = if (isToday) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = when {
                            day.isOverloaded -> "⚠"
                            day.taskCount > 0 -> "•".repeat(minOf(day.taskCount, 3))
                            else -> " "
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
