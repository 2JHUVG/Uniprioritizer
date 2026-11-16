package com.example.uniprioritizer.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.uniprioritizer.AppContainer
import com.example.uniprioritizer.UniPrioritizerApplication
import com.example.uniprioritizer.ui.auth.LoginViewModel
import com.example.uniprioritizer.ui.auth.RegisterViewModel
import com.example.uniprioritizer.ui.courses.CoursesViewModel
import com.example.uniprioritizer.ui.settings.SettingsViewModel
import com.example.uniprioritizer.ui.task.NewTaskViewModel
import com.example.uniprioritizer.ui.task.TaskDetailViewModel
import com.example.uniprioritizer.ui.today.TodayViewModel
import com.example.uniprioritizer.ui.week.WeekViewModel

/** Builds every ViewModel, passing the repositories from [AppContainer] through the constructor. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            MainViewModel(container().preferencesRepository, container().authRepository)
        }
        initializer { LoginViewModel(container().authRepository) }
        initializer { RegisterViewModel(container().authRepository) }
        initializer {
            TodayViewModel(
                container().taskRepository,
                container().authRepository,
                container().draftRepository
            )
        }
        initializer {
            WeekViewModel(container().taskRepository, container().preferencesRepository)
        }
        initializer {
            CoursesViewModel(container().courseRepository, container().taskRepository)
        }
        initializer {
            NewTaskViewModel(
                container().taskRepository,
                container().courseRepository,
                container().draftRepository
            )
        }
        initializer {
            TaskDetailViewModel(createSavedStateHandle(), container().taskRepository)
        }
        initializer {
            SettingsViewModel(
                container().preferencesRepository,
                container().authRepository,
                container().draftRepository
            )
        }
    }
}

private fun CreationExtras.container(): AppContainer =
    (this[APPLICATION_KEY] as UniPrioritizerApplication).container
