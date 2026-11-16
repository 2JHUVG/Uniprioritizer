package com.example.uniprioritizer.ui.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.uniprioritizer.domain.model.Course
import com.example.uniprioritizer.domain.model.Importance
import com.example.uniprioritizer.domain.model.TaskType
import com.example.uniprioritizer.ui.components.EmptyContent
import com.example.uniprioritizer.ui.components.ErrorContent
import com.example.uniprioritizer.ui.components.LoadingContent
import com.example.uniprioritizer.ui.components.OptionSelector
import com.example.uniprioritizer.ui.components.ValidatedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskScreen(
    uiState: NewTaskUiState,
    onNameChange: (String) -> Unit,
    onCourseSelect: (String) -> Unit,
    onDueDateChange: (String) -> Unit,
    onEstimatedHoursChange: (String) -> Unit,
    onTypeSelect: (TaskType) -> Unit,
    onImportanceSelect: (Importance) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onGoToCourses: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Nueva actividad") },
                navigationIcon = { TextButton(onClick = onCancel) { Text("Cancelar") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoadingCourses -> LoadingContent(message = "Cargando tus cursos…")
                uiState.coursesError != null -> ErrorContent(uiState.coursesError, onRetry)
                uiState.courses.isEmpty() -> EmptyContent(
                    title = "Primero crea un curso",
                    message = "Cada actividad pertenece a un curso. Crea uno y vuelve aquí.",
                    actionLabel = "Ir a Cursos",
                    onAction = onGoToCourses
                )
                else -> NewTaskForm(
                    uiState = uiState,
                    onNameChange = onNameChange,
                    onCourseSelect = onCourseSelect,
                    onDueDateChange = onDueDateChange,
                    onEstimatedHoursChange = onEstimatedHoursChange,
                    onTypeSelect = onTypeSelect,
                    onImportanceSelect = onImportanceSelect,
                    onSave = onSave
                )
            }
        }
    }
}

@Composable
private fun NewTaskForm(
    uiState: NewTaskUiState,
    onNameChange: (String) -> Unit,
    onCourseSelect: (String) -> Unit,
    onDueDateChange: (String) -> Unit,
    onEstimatedHoursChange: (String) -> Unit,
    onTypeSelect: (TaskType) -> Unit,
    onImportanceSelect: (Importance) -> Unit,
    onSave: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "draft_hint") {
            Text(
                text = "Lo que escribas se guarda como borrador en tu teléfono.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item(key = "name") {
            ValidatedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = "Nombre de la actividad",
                errorMessage = uiState.nameError,
                placeholder = "Laboratorio 8 de Física"
            )
        }
        item(key = "course") {
            CourseDropdown(uiState.courses, uiState.selectedCourse, onCourseSelect)
        }
        item(key = "type") {
            OptionSelector(
                title = "Tipo",
                options = TaskType.entries,
                selected = uiState.type,
                optionLabel = { it.label },
                onSelect = onTypeSelect
            )
        }
        item(key = "due_date") {
            ValidatedTextField(
                value = uiState.dueDate,
                onValueChange = onDueDateChange,
                label = "Fecha y hora de entrega",
                errorMessage = uiState.dueDateError,
                placeholder = "30/10/2026 18:30",
                supportingText = "Formato día/mes/año hora:minutos"
            )
        }
        item(key = "hours") {
            ValidatedTextField(
                value = uiState.estimatedHours,
                onValueChange = onEstimatedHoursChange,
                label = "Tiempo estimado (horas)",
                errorMessage = uiState.estimatedHoursError,
                placeholder = "1.5",
                keyboardType = KeyboardType.Decimal
            )
        }
        item(key = "importance") {
            OptionSelector(
                title = "Importancia",
                options = Importance.entries,
                selected = uiState.importance,
                optionLabel = { it.label },
                onSelect = onImportanceSelect
            )
        }
        uiState.saveError?.let { error ->
            item(key = "save_error") {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
        item(key = "save") {
            Button(
                onClick = onSave,
                enabled = uiState.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Guardar actividad")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseDropdown(
    courses: List<Course>,
    selectedCourse: Course?,
    onCourseSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedCourse?.name.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Curso") },
            placeholder = { Text("Elige un curso") },
            supportingText = { if (selectedCourse == null) Text("Selecciona el curso de la actividad") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            courses.forEach { course ->
                DropdownMenuItem(
                    text = { Text(course.name) },
                    onClick = {
                        onCourseSelect(course.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
