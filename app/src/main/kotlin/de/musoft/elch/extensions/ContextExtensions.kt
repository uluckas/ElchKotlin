package de.musoft.elch.extensions

import android.app.AlarmManager
import android.content.Context
import android.net.Uri
import android.os.PowerManager

/**
 * Created by luckas on 15.09.16.
 */

fun Context.uriForRessource(resourceId: Int) = Uri.Builder().
        scheme("android.resource").
        authority(packageName).
        appendPath(resourceId.toString()).
        build()

val Context.alarmManager: AlarmManager
    get() = getSystemService(Context.ALARM_SERVICE) as AlarmManager

val Context.powerManager: PowerManager
    get() = getSystemService(Context.POWER_SERVICE) as PowerManager

