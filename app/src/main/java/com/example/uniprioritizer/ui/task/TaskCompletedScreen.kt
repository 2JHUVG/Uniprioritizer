package com.example.uniprioritizer.ui.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.uniprioritizer.ui.common.PENDING_SYNC_MESSAGE

@Composable
fun TaskCompletedScreen(
    pendingSync: Boolean,
    onBackToToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            Text(
                text = "✓",
                modifier = Modifier.padding(18.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.headlineMedium
            )
        }
        Spacer(Modifier.height(18.dp))
        Text("¡Actividad completada!", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = if (pendingSync) PENDING_SYNC_MESSAGE else "Una menos. Tu lista de prioridades ya se actualizó.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onBackToToday) { Text("Volver a Hoy") }
    }
}
