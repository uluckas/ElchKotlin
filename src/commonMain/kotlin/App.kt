package de.musoft.elch.kmp.app

import androidx.compose.foundation.Image // Standard Compose Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Imports for Compose Multiplatform Resources
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

// A simple way to manage ViewModel lifecycle in common code for this example
// In a real app, use a proper KMP ViewModel library or platform-specific ViewModel handling.
@Composable
fun rememberTimerViewModel(): TimerViewModel = remember { TimerViewModel() }

@OptIn(ExperimentalResourceApi::class) // Opt-in for experimental resource API
@Composable
fun CommonApp() {
    val viewModel = rememberTimerViewModel()
    val timeToDisplay by viewModel.timeDisplay
    val isRunning by viewModel.isTimerRunning

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            try {
                Image(
                    painter = painterResource("drawable/elch.jpg"), // Path relative to commonMain/composeResources/
                    contentDescription = "Elch image",
                    modifier = Modifier.fillMaxWidth().height(200.dp) // Example modifier
                )
            } catch (e: Exception) {
                // Fallback if resource not found or other error
                Text(
                    "Error loading Elch image: ${e.message?.take(100)}", // Show limited error message
                    color = MaterialTheme.colors.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                // Also print to console for debugging
                println("Error loading elch.jpg: ${e.message}")
                // e.printStackTrace() // Avoid full stack trace in UI, already printed to console by default KMP behavior on error.
            }

            Text(
                text = timeToDisplay,
                fontSize = 48.sp,
                modifier = Modifier.padding(vertical = 20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { viewModel.onStartPauseClick() }) {
                    Text(if (isRunning) "Pause" else "Start")
                }
                Button(onClick = { viewModel.onResetClick() }) {
                    Text("Reset")
                }
            }
        }
    }

    // Handle ViewModel cleanup if the Composable is removed from composition
    // This is a simplified lifecycle management.
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clear()
        }
    }
}
