package de.musoft.elch.alarm

import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by luckas on 1/29/15.
 */

private const val S_IN_MS = 1000L

typealias SecondsChangedCallbackType = () -> Unit

class AlarmTimer(private val model: AlarmTimerModel,
                 private val alarmScheduler: Scheduler,
                 private val eventContext: CoroutineContext) {

    interface Scheduler {
        fun setAlarm(alarmTimeMS: Long)
        fun cancelAlarm()
    }

    private val secondsChangedCallbacks = ArrayList<SecondsChangedCallbackType>()
    private var secondsChangeTimerJob: Job? = null
    val remainingTimeS
        get() = model.computedRemainingTimeS

    init {
        model.remainingTimeMSEvent.add(this::onRemainingTimeMSChanged)
    }

    fun addSecondsListener(secondsChangedCallback: SecondsChangedCallbackType) {
        val hadNoListeners = secondsChangedCallbacks.isEmpty()
        secondsChangedCallbacks += secondsChangedCallback
        if (hadNoListeners && model.countdownRunning) {
            startSecondsChangeTimer()
        }
        // Give initial update to the secondsChangedCallback
        secondsChangedCallback()
    }

    fun removeSecondsListener(listener: SecondsChangedCallbackType) {
        secondsChangedCallbacks -= listener
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
        secondsChangeTimerJob?.cancel()
        secondsChangeTimerJob = null
    }

    private fun startSecondsChangeTimer() {
        secondsChangeTimerJob?.cancel()
        secondsChangeTimerJob = launch(eventContext) {
            while (true) {
                val remainingTimeMs = model.computedRemainingTimeMs
                if (remainingTimeMs <= 0L) {
                    break
                }
                val timeToNextSecondMS = remainingTimeMs % S_IN_MS
                delay(timeToNextSecondMS)
                fireSecondsChanged()
            }
        }
    }

    private fun onRemainingTimeMSChanged(oldValue: Long, newValue: Long) {
        fireSecondsChanged()
    }

    private fun fireSecondsChanged() {
        secondsChangedCallbacks.forEach { secondsChangedCallback ->
            secondsChangedCallback()
        }
    }
}
