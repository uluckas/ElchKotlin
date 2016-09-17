package de.musoft.elch.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import de.musoft.elch.broadcastreceivers.getAlarmIntent
import de.musoft.elch.delegates.BooleanSharedPreferenceShadow
import de.musoft.elch.delegates.LongSharedPreferenceShadow
import de.musoft.elch.extensions.alarmManager
import de.musoft.elch.extensions.mToMs
import de.musoft.elch.extensions.msToS
import java.util.*
import kotlin.reflect.KProperty

/**
 * Created by luckas on 1/29/15.
 */

private const val SPIELZEIT_DURATION_MIN: Long = 8L
private const val KEY_TIMER_RUNNING = "TIMER_RUNNING"
private const val KEY_ALARM_TIME_MS = "ALARM_TIME_MS"
private const val KEY_REMAINING_TIME_MS = "REMAINING_TIME_MS"

private const val S_IN_MS = 1000L

private val SPIELZEIT_DURATION_MS = SPIELZEIT_DURATION_MIN.mToMs()


class AlarmTimer(private val applicationContext: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val secondsChangedCallbacks = ArrayList<(Long) -> Unit>()
    private var secondsChangeTimer: Timer? = null
    private val alarmManager: AlarmManager = applicationContext.alarmManager
    private val sharedPreferences: SharedPreferences = applicationContext.getSharedPreferences("TimerState", Context.MODE_PRIVATE)
    private var alarmTimeMS: Long by LongSharedPreferenceShadow(sharedPreferences, KEY_ALARM_TIME_MS, 0)
    private var countdownRunning: Boolean by BooleanSharedPreferenceShadow(sharedPreferences, KEY_TIMER_RUNNING, false)

    private var remainingTimeMS: Long  by object : LongSharedPreferenceShadow(sharedPreferences, KEY_REMAINING_TIME_MS, SPIELZEIT_DURATION_MS) {
        override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
            super.setValue(thisRef, property, value)
            fireSecondsChanged()
        }
    }

    init {
        if (countdownRunning) {
            // If alarm time is in the past, the running timer already expired
            // If alarm time is too far in the future, the saved timer is probably from before the last boot
            if (alarmTimeMS <= SystemClock.elapsedRealtime() || alarmTimeMS > SystemClock.elapsedRealtime() + SPIELZEIT_DURATION_MS ) {
                countdownRunning = false
                remainingTimeMS = SPIELZEIT_DURATION_MS
            }
        } else {
            if (remainingTimeMS < 0 || remainingTimeMS > SPIELZEIT_DURATION_MS) {
                remainingTimeMS = SPIELZEIT_DURATION_MS
            }
        }
    }

    fun addSecondsListener(secondsChangedCallback: (Long) -> Unit) {
        val hadNoListeners = secondsChangedCallbacks.isEmpty()
        secondsChangedCallbacks.add(secondsChangedCallback)
        if (hadNoListeners && countdownRunning) {
            startSecondsChangeTimer()
        }
        // Give initial update to the secondsChangedCallback
        secondsChangedCallback(computedRemainigTimeS)
    }

    fun removeSecondsListener(listener: (Long) -> Unit) {
        secondsChangedCallbacks.remove(listener)
        if (secondsChangedCallbacks.isEmpty()) {
            cancelSecondsChangeTimer()
        }
    }

    fun startOrPauseCountdown() {
        if (countdownRunning) {
            pauseCountdown()
        } else {
            startCountdown(computedAlarmTimeMS)
        }
    }

    fun resetCountdown() {
        cancelSecondsChangeTimer()
        cancelAlarm()
        remainingTimeMS = SPIELZEIT_DURATION_MS
    }

    private val computedAlarmTimeMS: Long
        get() = if (countdownRunning) alarmTimeMS else SystemClock.elapsedRealtime() + remainingTimeMS

    private val computedRemainingTimeMS: Long
        get() {
            val computedRemainigTimeMs = if (countdownRunning) alarmTimeMS - SystemClock.elapsedRealtime() else remainingTimeMS
            return if (computedRemainigTimeMs < 0) 0 else computedRemainigTimeMs
        }

    private val computedRemainigTimeS: Long
        get() = (computedRemainingTimeMS + 500L).msToS()

    private fun pauseCountdown() {
        remainingTimeMS = computedRemainingTimeMS
        cancelAlarm()
        cancelSecondsChangeTimer()
    }

    private fun startCountdown(alarmTimeMS: Long) {
        setAlarm(alarmTimeMS)
        if (!secondsChangedCallbacks.isEmpty()) {
            startSecondsChangeTimer()
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
        countdownRunning = true
    }

    private fun cancelAlarm() {
        val pendingIntent = getAlarmIntent(applicationContext, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
        countdownRunning = false
    }

    private fun cancelSecondsChangeTimer() {
        secondsChangeTimer?.cancel()
        secondsChangeTimer = null
    }

    private fun startSecondsChangeTimer() {
        val newSecondsChangeTimer = Timer("SecondsChangeTimer", true)
        val onSecondChanged: TimerTask = object : TimerTask() {

            override fun run() {
                runOnMainThread {
                    if (computedRemainingTimeMS <= 0L) {
                        cancelSecondsChangeTimer()
                    }
                    fireSecondsChanged()
                }
            }

        }
        val timeNextSecondMS = computedRemainingTimeMS % S_IN_MS
        newSecondsChangeTimer.scheduleAtFixedRate(onSecondChanged, timeNextSecondMS, S_IN_MS)
        secondsChangeTimer = newSecondsChangeTimer
    }

    private fun fireSecondsChanged() {
        for (secondsChangedCallback in secondsChangedCallbacks) {
            secondsChangedCallback(computedRemainigTimeS)
        }
    }

    private fun runOnMainThread(function: () -> Unit) {
        mainHandler.post(function)
    }
}
