package de.musoft.elch.kmp.app

import com.russhwolf.settings.AppleSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

// The corresponding expect val applicationContextForSettings is not used on iOS,
// so no actual for it is needed here.

internal actual fun createSettings(): Settings {
    val userDefaults = NSUserDefaults.standardUserDefaults
    return AppleSettings(userDefaults)
}
