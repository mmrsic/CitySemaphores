package com.citysemaphores

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.citysemaphores.ui.screens.GameScreen
import com.citysemaphores.ui.theme.CitySemaphoresTheme
import com.citysemaphores.viewmodel.GameViewModel

@Composable
fun App() {
    val viewModel = remember { GameViewModel() }

    CitySemaphoresTheme {
        GameScreen(
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize()
        )
    }
}
