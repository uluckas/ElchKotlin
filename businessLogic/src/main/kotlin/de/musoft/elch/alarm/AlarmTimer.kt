package de.musoft.elch.alarm

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by luckas on 1/29/15.
 */

private const val S_IN_MS = 1000L

typealias SecondsChangedCallbackType = () -> Unit

class AlarmTimer(private val model: Model,
                 private val alarmScheduler: Scheduler,
                 private val eventContext: CoroutineContext) {

    interface Model {
        var secondsChangedCallback: SecondsChangedCallbackType?
        var countdownRunning: Boolean
        var alarmTimeMS: Long
        var remainingTimeMS: Long
        val computedRemainingTimeMs: Long
        val computedRemainingTimeS: Long
        val computedAlarmTimeMS: Long
        fun reset()
    }

    interface Scheduler {
        fun setAlarm(alarmTimeMS: Long)
        fun cancelAlarm()
    }

    private val secondsChangedCallbacks = ArrayList<SecondsChangedCallbackType>()
    private var secondsChangeTimer: Job? = null
    val remainingTimeS
        get() = model.computedRemainingTimeS

    init {
        model.secondsChangedCallback = this::fireSecondsChanged
    }

    fun addSecondsListener(secondsChangedCallback: SecondsChangedCallbackType) {
        val hadNoListeners = secondsChangedCallbacks.isEmpty()
        secondsChangedCallbacks.add(secondsChangedCallback)
        if (hadNoListeners && model.countdownRunning) {
            startSecondsChangeTimer()
        }
        // Give initial update to the secondsChangedCallback
        secondsChangedCallback()
    }

    fun removeSecondsListener(listener: SecondsChangedCallbackType) {
        secondsChangedCallbacks.remove(listener)
        if (secondsChangedCallbacks.isEmpty()) {
            cancelSecondsChangeTimer()
        }
    }

    fun startOrPauseCountdown() {
        if (model.countdownRunning) {
            pauseCountdown()
        } else {
            startCountdown(model.computedAlarmTimeMS)
        }
    }

    fun resetCountdown() {
        cancelSecondsChangeTimer()
        cancelAlarm()
        model.reset()
    }

    private fun pauseCountdown() {
        model.remainingTimeMS = model.computedRemainingTimeMs
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
        alarmScheduler.setAlarm(newAlarmTimeMS)
        model.alarmTimeMS = newAlarmTimeMS
        model.countdownRunning = true
    }

    private fun cancelAlarm() {
        alarmScheduler.cancelAlarm()
        model.countdownRunning = false
    }

    private fun cancelSecondsChangeTimer() {
        secondsChangeTimer?.cancel()
        secondsChangeTimer = null
    }

    private fun startSecondsChangeTimer() {
        secondsChangeTimer?.cancel()
        secondsChangeTimer = launch(eventContext) {
            while (true) {
                val remainingTimeMs = model.computedRemainingTimeMs
                if (remainingTimeMs <= 0L) {
                    break
                }
                val timeToNextSecondMS = remainingTimeMs % S_IN_MS
                delay(timeToNextSecondMS, TimeUnit.MILLISECONDS)
                fireSecondsChanged()
            }
        }
    }

    private fun fireSecondsChanged() {
        for (secondsChangedCallback in secondsChangedCallbacks) {
            secondsChangedCallback()
        }
    }
}
