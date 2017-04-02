package de.musoft.elch.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import de.musoft.elch.broadcastreceivers.getAlarmIntent
import de.musoft.elch.extensions.alarmManager

/**
 * Created by ulu on 31/03/17.
 */

class AlarmScheduler(private val applicationContext: Context) : AlarmTimer.Scheduler {

    override fun setAlarm(newAlarmTimeMS: Long) {
        val alarmManager = applicationContext.alarmManager
        val pendingIntent = getAlarmIntent(applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, newAlarmTimeMS, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, newAlarmTimeMS, pendingIntent)
        }
    }

    override fun cancelAlarm() {
        val alarmManager = applicationContext.alarmManager
        val pendingIntent = getAlarmIntent(applicationContext, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
    }

}