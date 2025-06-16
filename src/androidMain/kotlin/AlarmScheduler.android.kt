package de.musoft.elch.kmp.app // Match commonMain package for expect/actual

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

// Assumes 'applicationContextForSettings' actual var provides the Android Context.
// This was set up for multiplatform-settings.

private const val ALARM_ACTION = "de.musoft.elch.kmp.app.ALARM_FIRED"

private fun getPendingIntent(context: Context, flags: Int): PendingIntent {
    val intent = Intent(context, AlarmReceiver::class.java)
    intent.action = ALARM_ACTION
    return PendingIntent.getBroadcast(
        context,
        0, // requestCode
        intent,
        flags or PendingIntent.FLAG_IMMUTABLE // Add FLAG_IMMUTABLE for newer Android versions
    )
}

internal actual fun platformSetExactAlarm(alarmTimeMillis: Long) {
    val context = applicationContextForSettings as Context
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val pendingIntent = getPendingIntent(context, PendingIntent.FLAG_UPDATE_CURRENT)

    // alarmTimeMillis is based on TimeSource.Monotonic.
    // Android's AlarmManager.ELAPSED_REALTIME_WAKEUP also uses a similar monotonic clock.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTimeMillis, pendingIntent)
    } else {
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTimeMillis, pendingIntent)
    }
}

internal actual fun platformCancelAlarm() {
    val context = applicationContextForSettings as Context
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    // Use FLAG_NO_CREATE to check if an intent with matching parameters exists.
    // Pass PendingIntent.FLAG_IMMUTABLE as it was used during creation.
    val pendingIntent = getPendingIntent(context, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
    if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel() // Also cancel the PendingIntent itself
    }
}
