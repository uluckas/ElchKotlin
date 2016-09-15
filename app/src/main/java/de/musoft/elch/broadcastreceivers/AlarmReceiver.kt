package de.musoft.elch.broadcastreceivers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.musoft.elch.media.ring

private const val ELCH_ACTION = "de.musoft.elch.alarm.ELCH_ALARM"

public fun getAlarmIntent(context: Context, flags: Int): PendingIntent {
    val intent = Intent(ELCH_ACTION)
    intent.setClass(context, AlarmReceiver::class.java)
    return PendingIntent.getBroadcast(context, 0, intent, flags)
}

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        ring(context)
    }

}