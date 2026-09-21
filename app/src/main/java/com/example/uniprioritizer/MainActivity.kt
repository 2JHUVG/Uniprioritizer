package com.example.uniprioritizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.uniprioritizer.navigation.AppNavHost
import com.example.uniprioritizer.ui.theme.UniPrioritizerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniPrioritizerTheme {
                AppNavHost()
            }
        }
    }
}
