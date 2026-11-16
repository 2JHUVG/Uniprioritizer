package com.example.uniprioritizer.ui.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.uniprioritizer.domain.model.Importance
import com.example.uniprioritizer.domain.model.Task
import com.example.uniprioritizer.domain.model.priorityScore
import com.example.uniprioritizer.ui.components.EmptyContent
import com.example.uniprioritizer.ui.components.ErrorContent
import com.example.uniprioritizer.ui.components.LoadingContent
import com.example.uniprioritizer.ui.components.OfflineBanner
import com.example.uniprioritizer.ui.components.toHoursLabel
import com.example.uniprioritizer.ui.components.toShortLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    uiState: TaskDetailUiState,
    onBack: () -> Unit,
    onToggleCompleted: () -> Unit,
    onDeleteClick: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Detalle") },
                navigationIcon = { TextButton(onClick = onBack) { Text("‹ Atrás") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isOffline) OfflineBanner()
            when {
                uiState.isLoading -> LoadingContent(message = "Cargando la actividad…")
                uiState.errorMessage != null -> ErrorContent(uiState.errorMessage, onRetry)
                uiState.task == null -> EmptyContent(
                    title = "Actividad no encontrada",
                    message = "Es posible que se haya eliminado.",
                    actionLabel = "Volver",
                    onAction = onBack
                )
                else -> TaskDetailContent(
                    task = uiState.task,
                    isUpdating = uiState.isUpdating,
                    actionError = uiState.actionError,
                    onToggleCompleted = onToggleCompleted,
                    onDeleteClick = onDeleteClick
                )
            }
        }
    }
    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = onDeleteDismiss,
            title = { Text("¿Eliminar esta actividad?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = { TextButton(onClick = onDeleteConfirm) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = onDeleteDismiss) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun TaskDetailContent(
    task: Task,
    isUpdating: Boolean,
    actionError: String?,
    onToggleCompleted: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SuggestionChip(onClick = {}, label = { Text(task.type.label) })
            SuggestionChip(onClick = {}, label = { Text("Importancia ${task.importance.label}") })
            if (task.completed) SuggestionChip(onClick = {}, label = { Text("Completada") })
        }
        Text(task.name, style = MaterialTheme.typography.headlineSmall)
        Text("◈  ${task.courseName}", style = MaterialTheme.typography.bodyLarge)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("⚡ Puntaje de prioridad: ${task.priorityScore()}", style = MaterialTheme.typography.titleMedium)
                Text(priorityExplanation(task))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoCard("FECHA LÍMITE", task.dueDate.toShortLabel(), Modifier.weight(1f))
            InfoCard("TIEMPO ESTIMADO", task.estimatedHours.toHoursLabel(), Modifier.weight(1f))
        }
        actionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onToggleCompleted, enabled = !isUpdating, modifier = Modifier.fillMaxWidth()) {
            Text(if (task.completed) "Marcar como pendiente" else "Marcar como completada")
        }
        OutlinedButton(onClick = onDeleteClick, enabled = !isUpdating, modifier = Modifier.fillMaxWidth()) {
            Text("Eliminar actividad")
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    OutlinedCard(modifier = modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun priorityExplanation(task: Task): String {
    val importanceText = when (task.importance) {
        Importance.HIGH -> "Es de importancia alta"
        Importance.MEDIUM -> "Es de importancia media"
        Importance.LOW -> "Es de importancia baja"
    }
    return "$importanceText y necesita ${task.estimatedHours.toHoursLabel()}. " +
        "Mientras más cerca esté la entrega, más sube en tu lista."
}
