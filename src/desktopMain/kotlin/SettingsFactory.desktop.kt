package de.musoft.elch.kmp.app

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

// The corresponding expect val applicationContextForSettings is not used on desktop,
// so no actual for it is needed here.

internal actual fun createSettings(): Settings {
    val preferences = Preferences.userRoot()?.node("ElchAppKMP_settings")
    // PreferencesSettings expects a non-null java.util.prefs.Preferences.
    // Handle case where userRoot or node returns null if necessary, though typically they don't for userRoot.
    if (preferences == null) {
        // Fallback or error, though Preferences.userRoot() is generally reliable.
        // For simplicity, this example assumes it's non-null.
        // In a real app, provide a more robust fallback or error handling.
        throw IllegalStateException("Could not get Preferences node for settings")
    }
    return PreferencesSettings(preferences)
}
