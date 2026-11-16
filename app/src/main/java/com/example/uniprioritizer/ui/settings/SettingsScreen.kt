package com.example.uniprioritizer.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.uniprioritizer.domain.model.ThemeMode
import com.example.uniprioritizer.domain.model.WeekStart
import com.example.uniprioritizer.navigation.TopLevelDestination
import com.example.uniprioritizer.ui.components.AppBottomBar
import com.example.uniprioritizer.ui.components.OptionSelector

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onThemeModeSelect: (ThemeMode) -> Unit,
    onWeekStartSelect: (WeekStart) -> Unit,
    onSignOut: () -> Unit,
    onDestinationClick: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        bottomBar = { AppBottomBar(TopLevelDestination.SETTINGS, onDestinationClick) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Ajustes", style = MaterialTheme.typography.headlineSmall)
            Column {
                Text(uiState.userName.ifBlank { "Tu cuenta" }, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = uiState.userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HorizontalDivider()
            OptionSelector(
                title = "Tema",
                options = ThemeMode.entries,
                selected = uiState.themeMode,
                optionLabel = { it.label },
                onSelect = onThemeModeSelect
            )
            OptionSelector(
                title = "La semana empieza el",
                options = WeekStart.entries,
                selected = uiState.weekStart,
                optionLabel = { it.label },
                onSelect = onWeekStartSelect
            )
            Text(
                text = "Estas preferencias se guardan solo en este teléfono.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider()
            OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar sesión")
            }
        }
    }
}
