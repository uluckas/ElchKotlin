package de.musoft.elch.kmp.app

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

// The corresponding expect val is in commonMain/SettingsFactory.kt
// The actual val is in ActualAppContext.android.kt

internal actual fun createSettings(): Settings {
    // Cast 'applicationContextForSettings' to 'Context'
    val context = applicationContextForSettings as Context
    return SharedPreferencesSettings(context.getSharedPreferences("ElchAppKMP_settings", Context.MODE_PRIVATE))
}
