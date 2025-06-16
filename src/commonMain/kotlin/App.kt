package de.musoft.elch.kmp.app // Define a package for your common code

import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme // Added for a more complete example
import androidx.compose.runtime.Composable

@Composable
fun CommonApp() {
    MaterialTheme { // Wrap with MaterialTheme for basic styling
        Text("Hello, Common World!")
    }
}
