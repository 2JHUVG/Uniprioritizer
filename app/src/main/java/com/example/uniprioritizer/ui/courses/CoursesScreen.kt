package com.example.uniprioritizer.ui.courses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.uniprioritizer.domain.model.Course
import com.example.uniprioritizer.navigation.TopLevelDestination
import com.example.uniprioritizer.ui.components.AppBottomBar
import com.example.uniprioritizer.ui.components.EmptyContent
import com.example.uniprioritizer.ui.components.ErrorContent
import com.example.uniprioritizer.ui.components.LoadingContent
import com.example.uniprioritizer.ui.components.OfflineBanner
import com.example.uniprioritizer.ui.components.ValidatedTextField

@Composable
fun CoursesScreen(
    uiState: CoursesUiState,
    onQueryChange: (String) -> Unit,
    onAddCourseClick: () -> Unit,
    onCourseNameChange: (String) -> Unit,
    onCourseCodeChange: (String) -> Unit,
    onSaveCourse: () -> Unit,
    onCourseFormDismiss: () -> Unit,
    onDeleteCourseClick: (Course) -> Unit,
    onDeleteCourseConfirm: () -> Unit,
    onDeleteCourseDismiss: () -> Unit,
    onMessageShown: () -> Unit,
    onRetry: () -> Unit,
    onDestinationClick: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            onMessageShown()
        }
    }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { AppBottomBar(TopLevelDestination.COURSES, onDestinationClick) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddCourseClick) { Text("+ Nuevo curso") }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isOffline) OfflineBanner()
            when {
                uiState.isLoading -> LoadingContent(message = "Cargando tus cursos…")
                uiState.errorMessage != null -> ErrorContent(uiState.errorMessage, onRetry)
                else -> CoursesContent(uiState, onQueryChange, onAddCourseClick, onDeleteCourseClick)
            }
        }
    }
    uiState.courseForm?.let { form ->
        CourseFormDialog(form, onCourseNameChange, onCourseCodeChange, onSaveCourse, onCourseFormDismiss)
    }
    uiState.courseToDelete?.let { course ->
        AlertDialog(
            onDismissRequest = onDeleteCourseDismiss,
            title = { Text("¿Eliminar ${course.name}?") },
            text = { Text("También se eliminarán todas sus actividades. Esta acción no se puede deshacer.") },
            confirmButton = { TextButton(onClick = onDeleteCourseConfirm) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = onDeleteCourseDismiss) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun CoursesContent(
    uiState: CoursesUiState,
    onQueryChange: (String) -> Unit,
    onAddCourseClick: () -> Unit,
    onDeleteCourseClick: (Course) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "header") {
            Column {
                Text("Mis cursos", style = MaterialTheme.typography.headlineSmall)
                Text("${uiState.courses.size} cursos este semestre", style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (uiState.isEmpty) {
            item(key = "empty") {
                EmptyContent(
                    title = "Aún no tienes cursos",
                    message = "Crea tus cursos para poder asignarles tareas, proyectos y exámenes.",
                    actionLabel = "Crear mi primer curso",
                    onAction = onAddCourseClick
                )
            }
        } else {
            item(key = "search") {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Buscar curso o código…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            items(items = uiState.visibleCourses, key = { it.course.id }) { summary ->
                CourseRow(summary, onDeleteCourseClick)
            }
        }
    }
}

@Composable
private fun CourseRow(
    summary: CourseSummary,
    onDeleteCourseClick: (Course) -> Unit,
    modifier: Modifier = Modifier
) {
    val course = summary.course
    Column(modifier = modifier) {
        ListItem(
            headlineContent = { Text(course.name, style = MaterialTheme.typography.titleMedium) },
            supportingContent = {
                val code = if (course.code.isBlank()) "" else "${course.code} · "
                Text("$code${summary.pendingCount} pendientes")
            },
            leadingContent = {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(course.name.take(2).uppercase(), Modifier.padding(10.dp))
                }
            },
            trailingContent = {
                TextButton(onClick = { onDeleteCourseClick(course) }) { Text("Eliminar") }
            }
        )
        HorizontalDivider()
    }
}

@Composable
private fun CourseFormDialog(
    form: CourseForm,
    onNameChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo curso") },
        text = {
            Column {
                ValidatedTextField(
                    value = form.name,
                    onValueChange = onNameChange,
                    label = "Nombre del curso",
                    errorMessage = form.nameError,
                    placeholder = "Cálculo 2"
                )
                ValidatedTextField(
                    value = form.code,
                    onValueChange = onCodeChange,
                    label = "Código (opcional)",
                    errorMessage = form.codeError,
                    placeholder = "MM2010"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = form.canSave) {
                Text(if (form.isSaving) "Guardando…" else "Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
