package de.musoft.elch.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import de.musoft.elch.broadcastreceivers.AlarmReceiver
import de.musoft.elch.delegates.BooleanSharedPreferenceShadow
import de.musoft.elch.delegates.LongSharedPreferenceShadow
import de.musoft.elch.extensions.mToMs
import de.musoft.elch.extensions.msToS
import java.util.*
import kotlin.reflect.KProperty

/**
 * Created by luckas on 1/29/15.
 */

private const val ELCH_ACTION = "de.musoft.elch.alarm.ELCH_ALARM"
private const val SPIELZEIT_DURATION_MIN: Long = 8L
private const val KEY_TIMER_RUNNING = "TIMER_RUNNING"
private const val KEY_ALARM_TIME_MS = "ALARM_TIME_MS"
private const val KEY_REMAINING_TIME_MS = "REMAINING_TIME_MS"

private val SPIELZEIT_DURATION_MS = SPIELZEIT_DURATION_MIN.mToMs()

private fun getAlarmIntent(context: Context, flags: Int): PendingIntent {
    val intent = Intent(ELCH_ACTION)
    intent.setClass(context, AlarmReceiver::class.java)
    return PendingIntent.getBroadcast(context, 0, intent, flags)
}

class AlarmTimer(private val applicationContext: Context) {

    interface SecondsListener {

        fun onSecondsChanged(remainingSeconds: Long)

    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val listeners = ArrayList<SecondsListener>()
    private var secondsTimer: Timer? = null
    private val alarmManager: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val sharedPreferences: SharedPreferences = applicationContext.getSharedPreferences("TimerState", Context.MODE_PRIVATE)
    private var alarmTimeMS: Long by LongSharedPreferenceShadow(sharedPreferences, KEY_ALARM_TIME_MS, 0)
    private var timerRunning: Boolean by BooleanSharedPreferenceShadow(sharedPreferences, KEY_TIMER_RUNNING, false)

    private var remainingTimeMS: Long  by object : LongSharedPreferenceShadow(sharedPreferences, KEY_REMAINING_TIME_MS, SPIELZEIT_DURATION_MS) {
        override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
            super.setValue(thisRef, property, value)
            fireSecondsChanged()
        }
    }

    init {
        if (timerRunning) {
            // If alarm time is in the past, the running timer already expired
            // If alarm time is too far in the future, the saved timer is probably from before the last boot
            if (alarmTimeMS <= SystemClock.elapsedRealtime() || alarmTimeMS > SystemClock.elapsedRealtime() + SPIELZEIT_DURATION_MS ) {
                timerRunning = false
                remainingTimeMS = SPIELZEIT_DURATION_MS
            }
        } else {
            if (remainingTimeMS < 0 || remainingTimeMS > SPIELZEIT_DURATION_MS) {
                remainingTimeMS = SPIELZEIT_DURATION_MS
            }
        }
    }

    fun addSecondsListener(listener: SecondsListener) {
        val hadNoListeners = listeners.isEmpty()
        listeners.add(listener)
        if (hadNoListeners && timerRunning) {
            startSecondsTimer()
        }
        // Give initial update to the listener
        listener.onSecondsChanged(computedRemainigTimeS)
    }

    fun removeSecondsListener(listener: SecondsListener) {
        listeners.remove(listener)
        if (listeners.isEmpty()) {
            cancelSecondsTimer()
        }
    }

    fun startOrPauseTimer() {
        if (timerRunning) {
            pauseTimer()
        } else {
            startTimer(computedAlarmTimeMS)
        }
    }

    fun resetTimer() {
        cancelSecondsTimer()
        cancelAlarm()
        remainingTimeMS = SPIELZEIT_DURATION_MS
    }

    private val computedAlarmTimeMS: Long
        get() = if (timerRunning) alarmTimeMS else SystemClock.elapsedRealtime() + remainingTimeMS

    private val computedRemainingTimeMS: Long
        get() {
            val computedRemainigTimeMs = if (timerRunning) alarmTimeMS - SystemClock.elapsedRealtime() else remainingTimeMS
            return if (computedRemainigTimeMs < 0) 0 else computedRemainigTimeMs
        }

    private val computedRemainigTimeS: Long
        get() = (computedRemainingTimeMS + 500L).msToS()

    private fun pauseTimer() {
        remainingTimeMS = computedRemainingTimeMS
        cancelAlarm()
        cancelSecondsTimer()
    }

    private fun startTimer(alarmTimeMS: Long) {
        setAlarm(alarmTimeMS)
        if (!listeners.isEmpty()) {
            startSecondsTimer()
        }
    }

    private fun setAlarm(newAlarmTimeMS: Long) {
        val pendingIntent = getAlarmIntent(applicationContext, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, newAlarmTimeMS, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, newAlarmTimeMS, pendingIntent)
        }
        alarmTimeMS = newAlarmTimeMS
        timerRunning = true
    }

    private fun cancelAlarm() {
        val pendingIntent = getAlarmIntent(applicationContext, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
        timerRunning = false
    }

    private fun cancelSecondsTimer() {
        secondsTimer?.cancel()
        secondsTimer = null
    }

    private fun startSecondsTimer() {
        val nextFullSecond = computedRemainingTimeMS % 1000L
        val newSecondsTimer = Timer("Timer", true)
        newSecondsTimer.scheduleAtFixedRate(object: TimerTask() {

            override fun run() {
                mainHandler.post {
                    if (computedRemainingTimeMS <= 0L) {
                        cancelSecondsTimer()
                    }
                    fireSecondsChanged()
                }
            }
        }, nextFullSecond, 1000L)
        secondsTimer = newSecondsTimer
    }

    private fun fireSecondsChanged() {
        for (listener in listeners) {
            listener.onSecondsChanged(computedRemainigTimeS)
        }
    }

}
