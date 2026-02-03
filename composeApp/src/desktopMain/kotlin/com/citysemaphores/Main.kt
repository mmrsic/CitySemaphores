package com.citysemaphores

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp

fun main() = application {
    val windowState = rememberWindowState(
        width = 1280.dp,
        height = 800.dp
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "City Semaphores",
        state = windowState
    ) {
        App()
    }
}
