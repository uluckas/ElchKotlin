package de.musoft.elch.kmp.app // Consistent packaging

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController // Ensure this import is correct for KMP context

// This function will be called from Swift/Objective-C to get the main UIViewController
@Suppress("unused") // Suppress warning if not directly called from Kotlin in this module
fun MainViewController(): UIViewController = ComposeUIViewController { CommonApp() }
