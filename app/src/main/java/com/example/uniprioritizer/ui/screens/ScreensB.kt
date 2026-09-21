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
import androidx.compose.ui.unit.dp
import com.example.uniprioritizer.data.HOY_MOCK
import com.example.uniprioritizer.model.Actividad
import com.example.uniprioritizer.model.pendientesPorPrioridad
import com.example.uniprioritizer.viewmodel.ViewModelActivities
import java.time.format.DateTimeFormatter

private val Green = Color(0xFF405B3D)

@Composable
fun HoyScreen(viewModel: ViewModelActivities, onSemana: () -> Unit, onActividad: (Int) -> Unit, onNueva: () -> Unit, onCursos: () -> Unit = {}) {
    val actividades by viewModel.actividades.collectAsState()
    val pendientes = actividades.pendientesPorPrioridad()
    Scaffold(
        bottomBar = { AppBottomBar("Hoy", onHoy = {}, onSemana = onSemana, onCursos = onCursos) },
        floatingActionButton = { FloatingActionButton(onClick = onNueva, containerColor = Green) { Text("+", color = Color.White, style = MaterialTheme.typography.headlineSmall) } }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Spacer(Modifier.height(12.dp)); Text("Buenas tardes, Geovanni", style = MaterialTheme.typography.titleMedium) }
            item { Text("Empieza por aquí", style = MaterialTheme.typography.labelLarge); PriorityCard(pendientes.firstOrNull(), onActividad) }
            item { HorizontalDivider(); Text("Después", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
            items(pendientes.drop(1)) { ActivityRow(it, onActividad) }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun PriorityCard(activity: Actividad?, onActividad: (Int) -> Unit) {
    if (activity == null) return
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF132914)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(activity.materia.uppercase(), color = Color(0xFFBDD1B9), style = MaterialTheme.typography.labelSmall)
            Text(activity.nombre, color = Color.White, style = MaterialTheme.typography.titleLarge)
            Text("Prioridad ${activity.importancia.etiqueta.lowercase()}  •  ${activity.horasEstimadas} h", color = Color.White)
            Button(onClick = { onActividad(activity.id) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5F775B))) { Text("Ver detalle") }
        }
    }
}

@Composable
private fun ActivityRow(activity: Actividad, onActividad: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth().clickable { onActividad(activity.id) }.padding(vertical = 8.dp)) {
        Text(activity.tipo.etiqueta.uppercase(), style = MaterialTheme.typography.labelSmall, color = Green)
        Text(activity.nombre, fontWeight = FontWeight.SemiBold)
        Text("${activity.materia}  •  ${activity.fechaLimite.format(DateTimeFormatter.ofPattern("EEE HH:mm"))}", style = MaterialTheme.typography.bodySmall)
    }
    HorizontalDivider()
}

@Composable
fun SemanaScreen(viewModel: ViewModelActivities, onHoy: () -> Unit, onActividad: (Int) -> Unit, onNueva: () -> Unit, onCursos: () -> Unit = {}) {
    val actividades by viewModel.actividades.collectAsState()
    Scaffold(
        bottomBar = { AppBottomBar("Semana", onHoy, {}, onCursos) },
        floatingActionButton = { FloatingActionButton(onClick = onNueva, containerColor = Green) { Text("+", color = Color.White, style = MaterialTheme.typography.headlineSmall) } }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Spacer(Modifier.height(12.dp)); Text("Tu semana", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            item { WeekStrip() }
            item { AssistChip(onClick = {}, label = { Text("⚠ Miércoles concentra 5 h 15 min") }) }
            item { Text("Hoy · ${HOY_MOCK.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }} ${HOY_MOCK.dayOfMonth}") }
            items(actividades.pendientesPorPrioridad().sortedBy { it.fechaLimite }) { ActivityRow(it, onActividad) }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun WeekStrip() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        listOf("L\n21", "M\n22", "X\n23", "J\n24", "V\n25", "S\n26", "D\n27").forEachIndexed { index, day ->
            Surface(shape = RoundedCornerShape(20.dp), color = if (index == 0) Green else Color.Transparent) {
                Text(day, modifier = Modifier.padding(10.dp), color = if (index == 0) Color.White else Color.Unspecified)
            }
        }
    }
}

@Composable
fun AppBottomBar(selected: String, onHoy: () -> Unit, onSemana: () -> Unit, onCursos: (() -> Unit)? = null) {
    NavigationBar {
        NavigationBarItem(selected == "Hoy", onClick = onHoy, icon = { Text("▣") }, label = { Text("Hoy") })
        NavigationBarItem(selected == "Semana", onClick = onSemana, icon = { Text("▥") }, label = { Text("Semana") })
        NavigationBarItem(selected == "Cursos", onClick = { onCursos?.invoke() }, icon = { Text("◆") }, label = { Text("Cursos") }, enabled = onCursos != null)
    }
}
