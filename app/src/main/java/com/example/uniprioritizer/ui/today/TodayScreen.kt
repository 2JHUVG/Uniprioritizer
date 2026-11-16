package com.example.uniprioritizer.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.uniprioritizer.domain.model.Task
import com.example.uniprioritizer.navigation.TopLevelDestination
import com.example.uniprioritizer.ui.components.AppBottomBar
import com.example.uniprioritizer.ui.components.EmptyContent
import com.example.uniprioritizer.ui.components.ErrorContent
import com.example.uniprioritizer.ui.components.LoadingContent
import com.example.uniprioritizer.ui.components.OfflineBanner
import com.example.uniprioritizer.ui.components.TaskRow
import com.example.uniprioritizer.ui.components.toHoursLabel
import com.example.uniprioritizer.ui.components.toLongLabel
import com.example.uniprioritizer.ui.components.toShortLabel
import java.time.LocalDate
import java.time.LocalTime

private const val AFTERNOON_START_HOUR = 12
private const val EVENING_START_HOUR = 19

@Composable
fun TodayScreen(
    uiState: TodayUiState,
    onTaskClick: (Task) -> Unit,
    onNewTaskClick: () -> Unit,
    onDiscardDraft: () -> Unit,
    onRetry: () -> Unit,
    onDestinationClick: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        bottomBar = { AppBottomBar(TopLevelDestination.TODAY, onDestinationClick) },
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
                uiState.isLoading -> LoadingContent(message = "Cargando tus actividades…")
                uiState.errorMessage != null -> ErrorContent(uiState.errorMessage, onRetry)
                else -> TodayContent(uiState, onTaskClick, onNewTaskClick, onDiscardDraft)
            }
        }
    }
}

@Composable
private fun TodayContent(
    uiState: TodayUiState,
    onTaskClick: (Task) -> Unit,
    onNewTaskClick: () -> Unit,
    onDiscardDraft: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "greeting") {
            Column {
                Text(text = greeting(uiState.userName), style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = LocalDate.now().toLongLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        uiState.draftName?.let { draftName ->
            item(key = "draft") { DraftCard(draftName, onNewTaskClick, onDiscardDraft) }
        }
        if (uiState.isEmpty) {
            item(key = "empty") {
                EmptyContent(
                    title = "No tienes actividades pendientes",
                    message = "Agrega tu próxima tarea, proyecto o examen y te diremos por dónde empezar.",
                    actionLabel = "Agregar actividad",
                    onAction = onNewTaskClick
                )
            }
        } else {
            val firstTask = uiState.pendingTasks.first()
            item(key = "priority") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Empieza por aquí", style = MaterialTheme.typography.titleMedium)
                    PriorityCard(firstTask, onTaskClick)
                }
            }
            if (uiState.pendingTasks.size > 1) {
                item(key = "after_header") {
                    Text(
                        text = "Después",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(items = uiState.pendingTasks.drop(1), key = { it.id }) { task ->
                    TaskRow(task = task, onTaskClick = onTaskClick)
                }
            }
        }
    }
}

@Composable
private fun PriorityCard(
    task: Task,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = task.courseName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiaryContainer
            )
            Text(text = task.name, style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Importancia ${task.importance.label.lowercase()} · " +
                    "${task.estimatedHours.toHoursLabel()} · vence ${task.dueDate.toShortLabel()}"
            )
            Button(
                onClick = { onTaskClick(task) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text("Ver detalle")
            }
        }
    }
}

@Composable
private fun DraftCard(
    draftName: String,
    onContinueClick: () -> Unit,
    onDiscardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 4.dp)) {
            Text("BORRADOR GUARDADO", style = MaterialTheme.typography.labelSmall)
            Text(draftName, style = MaterialTheme.typography.bodyLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDiscardClick) { Text("Descartar") }
                TextButton(onClick = onContinueClick) { Text("Continuar") }
            }
        }
    }
}

private fun greeting(userName: String): String {
    val hour = LocalTime.now().hour
    val salutation = when {
        hour < AFTERNOON_START_HOUR -> "Buenos días"
        hour < EVENING_START_HOUR -> "Buenas tardes"
        else -> "Buenas noches"
    }
    val firstName = userName.trim().substringBefore(' ')
    return if (firstName.isEmpty()) salutation else "$salutation, $firstName"
}
