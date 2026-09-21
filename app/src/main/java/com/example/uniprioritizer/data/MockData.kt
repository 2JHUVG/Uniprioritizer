package com.example.uniprioritizer.data

import com.example.uniprioritizer.model.Actividad
import com.example.uniprioritizer.model.Importancia
import com.example.uniprioritizer.model.TipoActividad
import java.time.LocalDate
import java.time.LocalDateTime

val HOY_MOCK: LocalDate = LocalDate.of(2026, 9, 21)

val actividadesMock = listOf(
    Actividad(1, "Examen parcial de Cálculo 2", "Cálculo", TipoActividad.EXAMEN,
        LocalDateTime.of(2026, 9, 23, 8, 0), Importancia.ALTA, 3.0),
    Actividad(2, "Proyecto 3 de Plataformas Móviles", "Plataformas Móviles", TipoActividad.PROYECTO,
        LocalDateTime.of(2026, 9, 24, 23, 59), Importancia.ALTA, 6.0),
    Actividad(3, "Laboratorio 8 de Física", "Física", TipoActividad.TAREA,
        LocalDateTime.of(2026, 9, 23, 23, 59), Importancia.MEDIA, 2.0),
    Actividad(4, "Tarea de Matemática Discreta", "Matemática Discreta", TipoActividad.TAREA,
        LocalDateTime.of(2026, 9, 24, 18, 0), Importancia.MEDIA, 2.0),
    Actividad(5, "Hoja de trabajo de Física", "Física", TipoActividad.TAREA,
        LocalDateTime.of(2026, 9, 25, 12, 0), Importancia.BAJA, 1.5),
    Actividad(6, "Lectura de Ingeniería de Software", "Ingeniería de Software", TipoActividad.TAREA,
        LocalDateTime.of(2026, 9, 27, 20, 0), Importancia.BAJA, 1.0),
    Actividad(7, "Quiz de Matemática Discreta", "Matemática Discreta", TipoActividad.TAREA,
        LocalDateTime.of(2026, 9, 21, 10, 0), Importancia.MEDIA, 1.0, completada = true)
)