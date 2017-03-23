package de.musoft.elch.alarm

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import de.musoft.elch.delegates.BooleanSharedPreferenceShadow
import de.musoft.elch.delegates.LongSharedPreferenceShadow
import de.musoft.elch.extensions.mToMs
import de.musoft.elch.extensions.msToS
import kotlin.reflect.KProperty

/**
 * Created by luckas on 21.03.17.
 */

private const val KEY_TIMER_RUNNING = "TIMER_RUNNING"
private const val KEY_ALARM_TIME_MS = "ALARM_TIME_MS"
private const val KEY_REMAINING_TIME_MS = "REMAINING_TIME_MS"

private const val SPIELZEIT_DURATION_MIN: Long = 8L

private val SPIELZEIT_DURATION_MS = SPIELZEIT_DURATION_MIN.mToMs()

class AlarmTimerModel(private val applicationContext: Context, val secondsChangedCallback: () -> Unit) {
    private val sharedPreferences: SharedPreferences = applicationContext.getSharedPreferences("TimerState", Context.MODE_PRIVATE)
    internal var alarmTimeMS: Long by LongSharedPreferenceShadow(sharedPreferences, KEY_ALARM_TIME_MS, 0)
    internal var countdownRunning: Boolean by BooleanSharedPreferenceShadow(sharedPreferences, KEY_TIMER_RUNNING, false)

    internal var remainingTimeMS: Long  by object : LongSharedPreferenceShadow(sharedPreferences, KEY_REMAINING_TIME_MS, SPIELZEIT_DURATION_MS) {
        override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
            super.setValue(thisRef, property, value)
            secondsChangedCallback()
        }
    }

    internal val computedAlarmTimeMS: Long
        get() = if (countdownRunning) alarmTimeMS else SystemClock.elapsedRealtime() + remainingTimeMS

    internal val computedRemainingTimeMS: Long
        get() {
            val computedRemainigTimeMs = if (countdownRunning) alarmTimeMS - SystemClock.elapsedRealtime() else remainingTimeMS
            return if (computedRemainigTimeMs < 0) 0 else computedRemainigTimeMs
        }

    internal val computedRemainigTimeS: Long
        get() = (computedRemainingTimeMS + 500L).msToS()

    init {
        if (countdownRunning) {
            // If alarm time is in the past, the running timer already expired
            // If alarm time is too far in the future, the saved timer is probably from before the last boot
            if (alarmTimeMS <= SystemClock.elapsedRealtime() || alarmTimeMS > SystemClock.elapsedRealtime() + SPIELZEIT_DURATION_MS) {
                countdownRunning = false
                remainingTimeMS = SPIELZEIT_DURATION_MS
            }
        } else {
            if (remainingTimeMS < 0 || remainingTimeMS > SPIELZEIT_DURATION_MS) {
                remainingTimeMS = SPIELZEIT_DURATION_MS
            }
        }
    }

    fun reset() {
        remainingTimeMS = SPIELZEIT_DURATION_MS
    }

}

