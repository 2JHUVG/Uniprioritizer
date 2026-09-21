package com.example.uniprioritizer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.uniprioritizer.model.*
import com.example.uniprioritizer.viewmodel.ViewModelActivities
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val DarkGreen = Color(0xFF405B3D)

@Composable
fun CursosScreen(viewModel: ViewModelActivities, onHoy: () -> Unit, onSemana: () -> Unit, onNueva: () -> Unit) {
    val actividades by viewModel.actividades.collectAsState()
    val cursos = actividades.groupBy { it.materia }
    Scaffold(
        bottomBar = { AppBottomBar("Cursos", onHoy, onSemana, {}) },
        floatingActionButton = { FloatingActionButton(onClick = onNueva, containerColor = DarkGreen) { Text("+", color = Color.White) } }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Spacer(Modifier.height(12.dp)); Text("Mis Cursos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("${cursos.size} cursos activos en este semestre.") }
            item { OutlinedTextField(value = "", onValueChange = {}, enabled = false, placeholder = { Text("Buscar curso o código...") }, modifier = Modifier.fillMaxWidth()) }
            items(cursos.entries.toList()) { (curso, items) ->
                ListItem(
                    headlineContent = { Text(curso, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("${items.count { !it.completada }} pendientes") },
                    leadingContent = { Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE6ECE4)) { Text(curso.take(2).uppercase(), Modifier.padding(10.dp)) } },
                    trailingContent = { Text("›") }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun DetalleScreen(activityId: Int, viewModel: ViewModelActivities, onBack: () -> Unit, onCompletada: () -> Unit) {
    val actividades by viewModel.actividades.collectAsState()
    val activity = actividades.firstOrNull { it.id == activityId }
    if (activity == null) { LaunchedEffect(Unit) { onBack() }; return }
    Scaffold(topBar = { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("‹", Modifier.clickable(onClick = onBack)); Text("⋮") } }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { SuggestionChip(onClick = {}, label = { Text(activity.tipo.etiqueta) }); SuggestionChip(onClick = {}, label = { Text("Importancia ${activity.importancia.etiqueta}") }) }
            Text(activity.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("◈  ${activity.materia}")
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF132914))) { Column(Modifier.padding(16.dp)) { Text("⚡ Prioridad máxima", color = Color.White, fontWeight = FontWeight.Bold); Text("Esta actividad vence pronto y requiere ${activity.horasEstimadas} h para completarse.", color = Color.White) } }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoCard("FECHA LÍMITE", activity.fechaLimite.format(DateTimeFormatter.ofPattern("dd/MM, HH:mm")), Modifier.weight(1f))
                InfoCard("TIEMPO ESTIMADO", "${activity.horasEstimadas} h", Modifier.weight(1f))
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = onCompletada, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)) { Text("Marcar como completada") }
        }
    }
}

@Composable private fun InfoCard(title: String, value: String, modifier: Modifier) { OutlinedCard(modifier) { Column(Modifier.padding(14.dp)) { Text(title, style = MaterialTheme.typography.labelSmall); Text(value, fontWeight = FontWeight.SemiBold) } } }

@Composable
fun NuevaActividadScreen(onCancelar: () -> Unit, onGuardar: (Actividad) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var curso by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var horas by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TipoActividad.TAREA) }
    var importancia by remember { mutableStateOf(Importancia.MEDIA) }
    val fechaValida = remember(fecha) { runCatching { LocalDateTime.parse(fecha, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) }.getOrNull() }
    val horasValidas = horas.toDoubleOrNull()
    val valido = nombre.trim().length >= 3 && curso.isNotBlank() && fechaValida != null && horasValidas != null && horasValidas > 0
    Scaffold(topBar = { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { TextButton(onClick = onCancelar) { Text("Cancelar") }; Text("Nueva actividad", fontWeight = FontWeight.Bold); TextButton(enabled = valido, onClick = { onGuardar(Actividad(0, nombre.trim(), curso.trim(), tipo, fechaValida!!, importancia, horasValidas!!)) }) { Text("Guardar") } } }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { ValidatedField("Nombre de la actividad", nombre, { nombre = it }, nombre.isNotEmpty() && nombre.trim().length < 3, "Mínimo 3 caracteres") }
            item { ValidatedField("Curso", curso, { curso = it }, false, "") }
            item { Text("Tipo", style = MaterialTheme.typography.labelMedium); SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) { TipoActividad.entries.forEachIndexed { i, value -> SegmentedButton(selected = tipo == value, onClick = { tipo = value }, shape = SegmentedButtonDefaults.itemShape(i, TipoActividad.entries.size)) { Text(value.etiqueta) } } } }
            item { ValidatedField("Fecha y hora (dd/MM/yyyy HH:mm)", fecha, { fecha = it }, fecha.isNotBlank() && fechaValida == null, "Ejemplo: 30/09/2026 18:30") }
            item { OutlinedTextField(value = horas, onValueChange = { horas = it }, label = { Text("Tiempo estimado (horas)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = horas.isNotBlank() && (horasValidas == null || horasValidas <= 0), supportingText = { if (horas.isNotBlank() && (horasValidas == null || horasValidas <= 0)) Text("Ingresa un número mayor que cero") }, modifier = Modifier.fillMaxWidth()) }
            item { Text("Importancia", style = MaterialTheme.typography.labelMedium); SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) { Importancia.entries.forEachIndexed { i, value -> SegmentedButton(selected = importancia == value, onClick = { importancia = value }, shape = SegmentedButtonDefaults.itemShape(i, Importancia.entries.size)) { Text(value.etiqueta) } } } }
            item { Button(enabled = valido, onClick = { onGuardar(Actividad(0, nombre.trim(), curso.trim(), tipo, fechaValida!!, importancia, horasValidas!!)) }, modifier = Modifier.fillMaxWidth()) { Text("Guardar actividad") } }
        }
    }
}

@Composable private fun ValidatedField(label: String, value: String, onChange: (String) -> Unit, error: Boolean, message: String) { OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) }, isError = error, supportingText = { if (error) Text(message) }, modifier = Modifier.fillMaxWidth()) }

@Composable
fun CompletadaScreen(onVolver: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(shape = RoundedCornerShape(50), color = Color(0xFFDCEFD8)) { Text("✓", Modifier.padding(18.dp), color = DarkGreen, style = MaterialTheme.typography.headlineMedium) }
        Spacer(Modifier.height(18.dp)); Text("Informe completado", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Tu orden de trabajo fue actualizada correctamente en el sistema.")
        Spacer(Modifier.height(24.dp)); OutlinedButton(onClick = onVolver) { Text("Volver a Hoy") }
    }
}
