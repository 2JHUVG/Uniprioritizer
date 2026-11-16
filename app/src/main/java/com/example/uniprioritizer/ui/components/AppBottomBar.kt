package com.example.uniprioritizer.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.uniprioritizer.navigation.TopLevelDestination

@Composable
fun AppBottomBar(
    selected: TopLevelDestination,
    onDestinationClick: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        TopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = destination == selected,
                onClick = { if (destination != selected) onDestinationClick(destination) },
                icon = { Text(destination.symbol) },
                label = { Text(destination.label) }
            )
        }
    }
}
