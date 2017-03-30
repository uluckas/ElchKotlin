package de.musoft.elch.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import de.musoft.elch.broadcastreceivers.getAlarmIntent
import de.musoft.elch.extensions.alarmManager
import java.util.*

/**
 * Created by luckas on 1/29/15.
 */

private const val S_IN_MS = 1000L

typealias SecondsChangedCallbackType = (Long) -> Unit

class AlarmTimer(private val applicationContext: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val secondsChangedCallbacks = ArrayList<SecondsChangedCallbackType>()
    private var secondsChangeTimer: Timer? = null
    private val alarmManager: AlarmManager = applicationContext.alarmManager
    private val model = AlarmTimerModel(applicationContext) {
        fireSecondsChanged()
    }
    val remainingTime = model.computedRemainingTimeMS

    fun addSecondsListener(secondsChangedCallback: SecondsChangedCallbackType) {
        val hadNoListeners = secondsChangedCallbacks.isEmpty()
        secondsChangedCallbacks.add(secondsChangedCallback)
        if (hadNoListeners && model.countdownRunning) {
            startSecondsChangeTimer()
        }
        // Give initial update to the secondsChangedCallback
        secondsChangedCallback(model.computedRemainigTimeS)
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
        model.remainingTimeMS = model.computedRemainingTimeMS
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
        model.alarmTimeMS = newAlarmTimeMS
        model.countdownRunning = true
    }

    private fun cancelAlarm() {
        val pendingIntent = getAlarmIntent(applicationContext, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
        model.countdownRunning = false
    }

    private fun cancelSecondsChangeTimer() {
        secondsChangeTimer?.cancel()
        secondsChangeTimer = null
    }

    private fun startSecondsChangeTimer() {
        val newSecondsChangeTimer = Timer("SecondsChangeTimer", true)
        val onSecondChanged = object : TimerTask() {
            override fun run() {
                runOnMainThread {
                    if (model.computedRemainingTimeMS <= 0L) {
                        cancelSecondsChangeTimer()
                    }
                    fireSecondsChanged()
                }
            }

        }
        val timeNextSecondMS = model.computedRemainingTimeMS % S_IN_MS
        newSecondsChangeTimer.scheduleAtFixedRate(onSecondChanged, timeNextSecondMS, S_IN_MS)
        secondsChangeTimer = newSecondsChangeTimer
    }

    private fun fireSecondsChanged() {
        val computedRemainigTimeS = model.computedRemainigTimeS
        for (secondsChangedCallback in secondsChangedCallbacks) {
            secondsChangedCallback(computedRemainigTimeS)
        }
    }

    private fun runOnMainThread(function: () -> Unit) {
        mainHandler.post(function)
    }

}
