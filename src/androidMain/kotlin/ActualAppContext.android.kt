package de.musoft.elch.kmp.app
import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak") // App context is fine
internal actual lateinit var applicationContextForSettings: Any // Will be Context

// Call this from AndroidApp's onCreate
fun initAppContext(context: Context) {
    applicationContextForSettings = context.applicationContext
}
