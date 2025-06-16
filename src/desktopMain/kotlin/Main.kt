package de.musoft.elch.kmp.app // Consistent packaging

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
// MaterialTheme is now in CommonApp, so not strictly needed here for basic setup

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "ElchApp KMP Desktop") {
        CommonApp() // Call the common UI entry point
    }
}
