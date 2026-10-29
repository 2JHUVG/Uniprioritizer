package com.example.uniprioritizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uniprioritizer.navigation.AppNavHost
import com.example.uniprioritizer.ui.AppViewModelProvider
import com.example.uniprioritizer.ui.MainViewModel
import com.example.uniprioritizer.ui.theme.UniPrioritizerTheme

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels { AppViewModelProvider.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
            UniPrioritizerTheme(themeMode = uiState.themeMode) {
                AppNavHost(isSignedIn = uiState.isSignedIn)
            }
        }
    }
}
