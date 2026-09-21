package com.example.uniprioritizer.model

import com.example.uniprioritizer.data.HOY_MOCK
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

enum class Importancia(val peso: Int, val etiqueta: String) {
    ALTA(3, "Alta"), MEDIA(2, "Media"), BAJA(1, "Baja")
}

enum class TipoActividad(val etiqueta: String) {
    TAREA("Tarea"), PROYECTO("Proyecto"), EXAMEN("Examen")
}

data class Actividad(
    val id: Int,
    val nombre: String,
    val materia: String,
    val tipo: TipoActividad,
    val fechaLimite: LocalDateTime,
    val importancia: Importancia,
    val horasEstimadas: Double,
    val completada: Boolean = false
)

fun Actividad.puntaje(hoy: LocalDate = HOY_MOCK): Int {
    val dias = ChronoUnit.DAYS.between(hoy, fechaLimite.toLocalDate()).toInt()
    return importancia.peso * 10 + maxOf(0, 7 - dias)
}

fun List<Actividad>.pendientesPorPrioridad(): List<Actividad> =
    filter { !it.completada }
        .sortedWith(compareByDescending<Actividad> { it.puntaje() }.thenBy { it.fechaLimite })

fun List<Actividad>.delDia(dia: LocalDate): List<Actividad> =
    filter { !it.completada && it.fechaLimite.toLocalDate() == dia }

fun List<Actividad>.esSobrecargado(dia: LocalDate): Boolean {
    val delDia = delDia(dia)
    return delDia.sumOf { it.horasEstimadas } >= 6.0 || delDia.size >= 3
}