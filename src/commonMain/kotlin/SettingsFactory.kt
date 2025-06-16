package de.musoft.elch.kmp.app
import com.russhwolf.settings.Settings

// This is the primary expect declaration for the settings factory
internal expect fun createSettings(): Settings

// This is the expect declaration for the Android context provider
// It could also live in a separate AppContext.kt in commonMain
internal expect val applicationContextForSettings: Any
