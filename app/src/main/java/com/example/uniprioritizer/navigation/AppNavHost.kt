package com.example.uniprioritizer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.uniprioritizer.ui.screens.CursosScreen
import com.example.uniprioritizer.ui.screens.DetalleScreen
import com.example.uniprioritizer.ui.screens.NuevaActividadScreen
import com.example.uniprioritizer.ui.screens.CompletadaScreen
import com.example.uniprioritizer.ui.screens.HoyScreen
import com.example.uniprioritizer.ui.screens.SemanaScreen
import com.example.uniprioritizer.viewmodel.ViewModelActivities

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val activitiesViewModel: ViewModelActivities = viewModel()

    NavHost(navController = navController, startDestination = Routes.HOY, modifier = modifier) {
        composable(Routes.HOY) {
            HoyScreen(
                viewModel = activitiesViewModel,
                onSemana = { navController.navigate(Routes.SEMANA) },
                onActividad = { navController.navigate(Routes.detalle(it)) },
                onNueva = { navController.navigate(Routes.NUEVA) },
                onCursos = { navController.navigate(Routes.CURSOS) }
            )
        }
        composable(Routes.SEMANA) {
            SemanaScreen(
                viewModel = activitiesViewModel,
                onHoy = { navController.navigate(Routes.HOY) { launchSingleTop = true } },
                onActividad = { navController.navigate(Routes.detalle(it)) },
                onNueva = { navController.navigate(Routes.NUEVA) },
                onCursos = { navController.navigate(Routes.CURSOS) }
            )
        }
        composable(Routes.CURSOS) {
            CursosScreen(
                viewModel = activitiesViewModel,
                onHoy = { navController.navigate(Routes.HOY) },
                onSemana = { navController.navigate(Routes.SEMANA) },
                onNueva = { navController.navigate(Routes.NUEVA) }
            )
        }
        composable(Routes.NUEVA) {
            NuevaActividadScreen(
                onCancelar = { navController.popBackStack() },
                onGuardar = { nueva -> activitiesViewModel.agregar(nueva); navController.navigate(Routes.HOY) }
            )
        }
        composable(Routes.DETALLE, arguments = listOf(navArgument(Routes.DETALLE_ARG) { type = NavType.IntType })) { entry ->
            val id = entry.arguments?.getInt(Routes.DETALLE_ARG) ?: return@composable
            DetalleScreen(
                activityId = id,
                viewModel = activitiesViewModel,
                onBack = { navController.popBackStack() },
                onCompletada = { activitiesViewModel.marcarCompletada(id); navController.navigate(Routes.COMPLETADA) }
            )
        }
        composable(Routes.COMPLETADA) {
            CompletadaScreen(onVolver = { navController.navigate(Routes.HOY) { popUpTo(Routes.HOY) { inclusive = false } } })
        }
    }
}
