package de.musoft.elch.kmp.app // Adjusted package for common module

// TODO KMP: Remove direct Android Context dependency if still present implicitly via other old code.
// import android.app.AlarmManager // Replaced by expect fun
// import android.app.PendingIntent // Replaced by expect fun
// import android.content.Context // Replaced by expect fun or passed via expect fun params if absolutely needed
// import android.content.Intent // Replaced by expect fun
// import android.os.Build // Replaced by expect fun logic

import com.russhwolf.settings.Settings
import com.russhwolf.settings.long // Extension for getLong/putLong
import com.russhwolf.settings.boolean // Extension for getBoolean/putBoolean

import kotlin.reflect.KProperty // This is fine.

import kotlin.time.TimeSource
import kotlin.time.DurationUnit
import kotlin.time.toDuration // For converting Long to Duration
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

import de.musoft.elch.kmp.app.common.utils.mToMs // Import moved extensions
import de.musoft.elch.kmp.app.common.utils.msToS // Import moved extensions

// Expected platform functions for alarm scheduling
internal expect fun platformSetExactAlarm(alarmTimeMillis: Long)
internal expect fun platformCancelAlarm()

// TODO KMP: Constants might need review based on how features are implemented in KMP.
// ELCH_ACTION was for Android Intents. It might be useful in androidActual.
private const val ELCH_ACTION = "de.musoft.elch.alarm.ELCH_ALARM"
private const val SPIELZEIT_DURATION_MIN: Long = 8L
private const val KEY_TIMER_RUNNING = "TIMER_RUNNING"
private const val KEY_ALARM_TIME_MS = "ALARM_TIME_MS"
private const val KEY_REMAINING_TIME_MS = "REMAINING_TIME_MS"

// Removed private fun Long.mToMs(): Long = this * 60 * 1000
// Removed private fun Long.msToS(): Long = this / 1000

private val SPIELZEIT_DURATION_MS = SPIELZEIT_DURATION_MIN.mToMs()

class CommonAlarmTimer(
    private val settings: Settings,
    private val coroutineScope: CoroutineScope
) {

    private val secondsChangedCallbacks = ArrayList<(Long) -> Unit>()
    private var secondsTimerJob: Job? = null

    private var alarmTimeMS: Long by settings.long(KEY_ALARM_TIME_MS, 0L)
    private var timerRunning: Boolean by settings.boolean(KEY_TIMER_RUNNING, false)

    private var _remainingTimeMSInternal: Long
        get() = settings.getLong(KEY_REMAINING_TIME_MS, SPIELZEIT_DURATION_MS)
        set(value) {
            settings.putLong(KEY_REMAINING_TIME_MS, value)
            fireSecondsChanged()
        }

    private var remainingTimeMS: Long
        get() = _remainingTimeMSInternal
        set(value) { _remainingTimeMSInternal = value }

    private fun currentMonotonicTimeMs(): Long = TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds

    init {
        if (timerRunning) {
            val currentTime = currentMonotonicTimeMs()
            if (alarmTimeMS <= currentTime || alarmTimeMS > currentTime + SPIELZEIT_DURATION_MS) {
                timerRunning = false
                _remainingTimeMSInternal = SPIELZEIT_DURATION_MS
            } else {
                 _remainingTimeMSInternal = alarmTimeMS - currentTime
            }
        } else {
            if (_remainingTimeMSInternal < 0 || _remainingTimeMSInternal > SPIELZEIT_DURATION_MS) {
                _remainingTimeMSInternal = SPIELZEIT_DURATION_MS
            }
        }
        fireSecondsChanged()
    }

    fun addSecondsListener(secondsChangedCallback: (Long) -> Unit) {
        val hadNoListeners = secondsChangedCallbacks.isEmpty()
        secondsChangedCallbacks.add(secondsChangedCallback)
        if (hadNoListeners && timerRunning) {
            startSecondsTimer()
        }
        secondsChangedCallback(computedRemainigTimeS)
    }

    fun removeSecondsListener(listener: (Long) -> Unit) {
        secondsChangedCallbacks.remove(listener)
        if (secondsChangedCallbacks.isEmpty()) {
            cancelSecondsTimer()
        }
    }

    fun startOrPauseTimer() {
        if (timerRunning) {
            pauseTimer()
        } else {
            val newAlarmTime = currentMonotonicTimeMs() + _remainingTimeMSInternal
            startTimer(newAlarmTime)
        }
    }

    fun resetTimer() {
        cancelSecondsTimer()
        cancelAlarm()
        _remainingTimeMSInternal = SPIELZEIT_DURATION_MS
    }

    private val computedAlarmTimeMS: Long
        get() = if (timerRunning) alarmTimeMS else currentMonotonicTimeMs() + _remainingTimeMSInternal

    private val computedRemainingTimeMS: Long
        get() {
            val currentTime = currentMonotonicTimeMs()
            val computedMs = if (timerRunning) alarmTimeMS - currentTime else _remainingTimeMSInternal
            return if (computedMs < 0) 0 else computedMs
        }

    private val computedRemainigTimeS: Long
        get() = (computedRemainingTimeMS + 500L).msToS()

    private fun pauseTimer() {
        _remainingTimeMSInternal = computedRemainingTimeMS
        cancelAlarm()
        cancelSecondsTimer()
    }

    private fun startTimer(newAlarmTimeMs: Long) {
        setAlarm(newAlarmTimeMs)
        if (!secondsChangedCallbacks.isEmpty()) {
            startSecondsTimer()
        }
    }

    private fun setAlarm(newAlarmTimeMSParam: Long) {
        platformSetExactAlarm(newAlarmTimeMSParam)
        this.alarmTimeMS = newAlarmTimeMSParam
        timerRunning = true // Assume platform call was successful. Actual state might need callback.
    }

    private fun cancelAlarm() {
        platformCancelAlarm()
        timerRunning = false // Assume platform call was successful. Actual state might need callback.
    }

    private fun cancelSecondsTimer() {
        secondsTimerJob?.cancel()
        secondsTimerJob = null
    }

    private fun startSecondsTimer() {
        cancelSecondsTimer()
        secondsTimerJob = coroutineScope.launch {
            flow {
                while (isActive) {
                    emit(Unit)
                    delay(1000L.toDuration(DurationUnit.MILLISECONDS))
                }
            }
            .conflate()
            .collect {
                if (computedRemainingTimeMS <= 0L) {
                    _remainingTimeMSInternal = 0L
                    fireSecondsChanged()
                    pauseTimer()
                } else {
                    fireSecondsChanged()
                }
                if (!timerRunning) {
                    cancelSecondsTimer()
                }
            }
        }
    }

    private fun fireSecondsChanged() {
        for (secondsChangedCallback in secondsChangedCallbacks) {
            secondsChangedCallback(computedRemainigTimeS)
        }
    }
}
