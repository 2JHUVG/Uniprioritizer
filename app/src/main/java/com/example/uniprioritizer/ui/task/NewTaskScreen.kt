package com.example.uniprioritizer.ui.task

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private const val DEFAULT_DUE_HOUR = 23
private const val DEFAULT_DUE_MINUTE = 59

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
            DueDateField(
                value = uiState.dueDate,
                errorMessage = uiState.dueDateError,
                onDueDateChange = onDueDateChange
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDateField(
    value: String,
    errorMessage: String?,
    onDueDateChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = remember { DateTimeFormatter.ofPattern(DUE_DATE_PATTERN) }
    val current = remember(value) {
        runCatching { LocalDateTime.parse(value, formatter) }.getOrNull()
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var pickedDate by remember { mutableStateOf<LocalDate?>(null) }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha y hora de entrega") },
            placeholder = { Text("Toca para elegir") },
            isError = errorMessage != null,
            supportingText = { Text(errorMessage ?: "Toca para elegir la fecha y la hora") },
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showDatePicker = true }
        )
    }

    if (showDatePicker) {
        val today = LocalDate.now()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (current?.toLocalDate() ?: today)
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    !Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate().isBefore(today)
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickedDate = datePickerState.selectedDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        showDatePicker = false
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) { Text("Siguiente") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    pickedDate?.let { date ->
        val timePickerState = rememberTimePickerState(
            initialHour = current?.hour ?: DEFAULT_DUE_HOUR,
            initialMinute = current?.minute ?: DEFAULT_DUE_MINUTE,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { pickedDate = null },
            title = { Text("Hora de entrega") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val dueDate = date.atTime(timePickerState.hour, timePickerState.minute)
                        onDueDateChange(dueDate.format(formatter))
                        pickedDate = null
                    }
                ) { Text("Listo") }
            },
            dismissButton = { TextButton(onClick = { pickedDate = null }) { Text("Cancelar") } }
        )
    }
}
