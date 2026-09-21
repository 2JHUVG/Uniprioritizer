package com.example.uniprioritizer.navigation

object Routes {
    const val HOY = "hoy"
    const val SEMANA = "semana"
    const val NUEVA = "nueva"
    const val DETALLE_ARG = "actividadId"
    const val DETALLE = "detalle/{$DETALLE_ARG}"
    const val CURSOS = "cursos"
    const val COMPLETADA = "completada"
    fun detalle(id: Int) = "detalle/$id"
}
