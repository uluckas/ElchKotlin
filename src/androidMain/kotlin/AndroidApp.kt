package de.musoft.elch.kmp.app // Match the namespace in build.gradle.kts and AndroidManifest

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity // Or androidx.activity.ComponentActivity
// MaterialTheme is now in CommonApp, so not strictly needed here unless for Android-specific theming
import io.fabric.sdk.android.Fabric
import com.crashlytics.android.Crashlytics

class AndroidApp : AppCompatActivity() { // AppCompatActivity for wider compatibility, or ComponentActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Fabric with Crashlytics
        Fabric.with(Fabric.Builder(this)
            .kits(Crashlytics()) // Assuming Beta kit is not strictly needed for basic crash reporting
            .build())

        setContent {
            CommonApp() // Call the common UI entry point
        }
    }
}
