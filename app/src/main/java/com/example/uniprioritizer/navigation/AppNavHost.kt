package com.example.uniprioritizer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.uniprioritizer.domain.model.WriteStatus
import com.example.uniprioritizer.ui.AppViewModelProvider
import com.example.uniprioritizer.ui.auth.LoginScreen
import com.example.uniprioritizer.ui.auth.LoginViewModel
import com.example.uniprioritizer.ui.auth.RegisterScreen
import com.example.uniprioritizer.ui.auth.RegisterViewModel
import com.example.uniprioritizer.ui.courses.CoursesScreen
import com.example.uniprioritizer.ui.courses.CoursesViewModel
import com.example.uniprioritizer.ui.settings.SettingsScreen
import com.example.uniprioritizer.ui.settings.SettingsViewModel
import com.example.uniprioritizer.ui.task.NewTaskScreen
import com.example.uniprioritizer.ui.task.NewTaskViewModel
import com.example.uniprioritizer.ui.task.TaskCompletedScreen
import com.example.uniprioritizer.ui.task.TaskDetailScreen
import com.example.uniprioritizer.ui.task.TaskDetailViewModel
import com.example.uniprioritizer.ui.today.TodayScreen
import com.example.uniprioritizer.ui.today.TodayViewModel
import com.example.uniprioritizer.ui.week.WeekScreen
import com.example.uniprioritizer.ui.week.WeekViewModel

@Composable
fun AppNavHost(
    isSignedIn: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val onDestinationClick: (TopLevelDestination) -> Unit = { destination ->
        navController.navigate(destination.route) {
            popUpTo(Routes.TODAY) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    val goToToday = {
        navController.navigate(Routes.TODAY) {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isSignedIn) Routes.TODAY else Routes.LOGIN,
        modifier = modifier
    ) {
        composable(Routes.LOGIN) {
            val viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(uiState.isSignedIn) { if (uiState.isSignedIn) goToToday() }
            LoginScreen(
                uiState = uiState,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onSubmit = viewModel::onSubmit,
                onRegisterClick = { navController.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) {
            val viewModel: RegisterViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(uiState.isRegistered) { if (uiState.isRegistered) goToToday() }
            RegisterScreen(
                uiState = uiState,
                onNameChange = viewModel::onNameChange,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                onSubmit = viewModel::onSubmit,
                onLoginClick = { navController.popBackStack() }
            )
        }
        composable(Routes.TODAY) {
            val viewModel: TodayViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            TodayScreen(
                uiState = uiState,
                onTaskClick = { navController.navigate(Routes.taskDetail(it.courseId, it.id)) },
                onNewTaskClick = { navController.navigate(Routes.NEW_TASK) },
                onDiscardDraft = viewModel::onDiscardDraft,
                onRetry = viewModel::onRetry,
                onDestinationClick = onDestinationClick
            )
        }
        composable(Routes.WEEK) {
            val viewModel: WeekViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            WeekScreen(
                uiState = uiState,
                onDaySelect = viewModel::onDaySelect,
                onPreviousWeek = viewModel::onPreviousWeek,
                onNextWeek = viewModel::onNextWeek,
                onTaskClick = { navController.navigate(Routes.taskDetail(it.courseId, it.id)) },
                onNewTaskClick = { navController.navigate(Routes.NEW_TASK) },
                onRetry = viewModel::onRetry,
                onDestinationClick = onDestinationClick
            )
        }
        composable(Routes.COURSES) {
            val viewModel: CoursesViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            CoursesScreen(
                uiState = uiState,
                onQueryChange = viewModel::onQueryChange,
                onAddCourseClick = viewModel::onAddCourseClick,
                onCourseNameChange = viewModel::onCourseNameChange,
                onCourseCodeChange = viewModel::onCourseCodeChange,
                onSaveCourse = viewModel::onSaveCourse,
                onCourseFormDismiss = viewModel::onCourseFormDismiss,
                onDeleteCourseClick = viewModel::onDeleteCourseClick,
                onDeleteCourseConfirm = viewModel::onDeleteCourseConfirm,
                onDeleteCourseDismiss = viewModel::onDeleteCourseDismiss,
                onMessageShown = viewModel::onMessageShown,
                onRetry = viewModel::onRetry,
                onDestinationClick = onDestinationClick
            )
        }
        composable(Routes.SETTINGS) {
            val viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(uiState.isSignedOut) {
                if (uiState.isSignedOut) {
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                }
            }
            SettingsScreen(
                uiState = uiState,
                onThemeModeSelect = viewModel::onThemeModeSelect,
                onWeekStartSelect = viewModel::onWeekStartSelect,
                onSignOut = viewModel::onSignOut,
                onDestinationClick = onDestinationClick
            )
        }
        composable(Routes.NEW_TASK) {
            val viewModel: NewTaskViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(uiState.savedStatus) {
                if (uiState.savedStatus != null) navController.popBackStack()
            }
            NewTaskScreen(
                uiState = uiState,
                onNameChange = viewModel::onNameChange,
                onCourseSelect = viewModel::onCourseSelect,
                onDueDateChange = viewModel::onDueDateChange,
                onEstimatedHoursChange = viewModel::onEstimatedHoursChange,
                onTypeSelect = viewModel::onTypeSelect,
                onImportanceSelect = viewModel::onImportanceSelect,
                onSave = viewModel::onSave,
                onCancel = { navController.popBackStack() },
                onGoToCourses = {
                    navController.popBackStack()
                    onDestinationClick(TopLevelDestination.COURSES)
                },
                onRetry = viewModel::onRetry
            )
        }
        composable(
            route = Routes.TASK_DETAIL,
            arguments = listOf(
                navArgument(Routes.COURSE_ID_ARG) { type = NavType.StringType },
                navArgument(Routes.TASK_ID_ARG) { type = NavType.StringType }
            )
        ) {
            val viewModel: TaskDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(uiState.completedStatus, uiState.isDeleted) {
                val completedStatus = uiState.completedStatus
                when {
                    completedStatus != null -> {
                        viewModel.onNavigationHandled()
                        navController.navigate(
                            Routes.taskCompleted(completedStatus == WriteStatus.PENDING_SYNC)
                        ) {
                            popUpTo(Routes.TASK_DETAIL) { inclusive = true }
                        }
                    }
                    uiState.isDeleted -> {
                        viewModel.onNavigationHandled()
                        navController.popBackStack()
                    }
                }
            }
            TaskDetailScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onToggleCompleted = viewModel::onToggleCompleted,
                onDeleteClick = viewModel::onDeleteClick,
                onDeleteConfirm = viewModel::onDeleteConfirm,
                onDeleteDismiss = viewModel::onDeleteDismiss,
                onRetry = viewModel::onRetry
            )
        }
        composable(
            route = Routes.TASK_COMPLETED,
            arguments = listOf(navArgument(Routes.PENDING_SYNC_ARG) { type = NavType.BoolType })
        ) { entry ->
            TaskCompletedScreen(
                pendingSync = entry.arguments?.getBoolean(Routes.PENDING_SYNC_ARG) ?: false,
                onBackToToday = {
                    navController.navigate(Routes.TODAY) {
                        popUpTo(Routes.TODAY) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
