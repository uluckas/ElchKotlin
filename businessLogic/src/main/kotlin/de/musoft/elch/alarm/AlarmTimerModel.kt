package de.musoft.elch.alarm

import de.musoft.elch.delegates.KeyValueStore
import de.musoft.elch.delegates.KeyValueStorePropertyDelegate
import de.musoft.elch.platform.TimeSource
import kotlin.reflect.KProperty

/**
 * Created by luckas on 21.03.17.
 */

private const val KEY_TIMER_RUNNING = "TIMER_RUNNING"
private const val KEY_ALARM_TIME_MS = "ALARM_TIME_MS"
private const val KEY_REMAINING_TIME_MS = "REMAINING_TIME_MS"

private const val SPIELZEIT_DURATION_MIN: Long = 8L

private val SPIELZEIT_DURATION_MS = SPIELZEIT_DURATION_MIN * 60 * 1000

class AlarmTimerModel(booleanStore: KeyValueStore<String, Boolean>,
                      longStore: KeyValueStore<String, Long>,
                      val timeSource: TimeSource) : AlarmTimer.Model {

    override var secondsChangedCallback: SecondsChangedCallbackType? = null
    override var alarmTimeMS: Long by KeyValueStorePropertyDelegate(longStore, KEY_ALARM_TIME_MS, 0)
    override var countdownRunning: Boolean by KeyValueStorePropertyDelegate(booleanStore, KEY_TIMER_RUNNING, false)

    override var remainingTimeMS: Long by object : KeyValueStorePropertyDelegate<String, Long>(longStore, KEY_REMAINING_TIME_MS, SPIELZEIT_DURATION_MS) {
        override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
            super.setValue(thisRef, property, value)
            secondsChangedCallback?.invoke()
        }
    }

    override val computedAlarmTimeMS: Long
        get() = if (countdownRunning) alarmTimeMS else getTimeMs() + remainingTimeMS

    override val computedRemainingTimeMs: Long
        get() {
            val computedRemainigTimeMs = if (countdownRunning) alarmTimeMS - getTimeMs() else remainingTimeMS
            return if (computedRemainigTimeMs < 0) 0 else computedRemainigTimeMs
        }

    override val computedRemainingTimeS: Long
        get() = (computedRemainingTimeMs + 500L) / 1000L

    init {
        if (countdownRunning) {
            // If alarm time is in the past, the running timer already expired
            // If alarm time is too far in the future, the saved timer is probably from before the last boot
            if (alarmTimeMS <= getTimeMs() || alarmTimeMS > getTimeMs() + SPIELZEIT_DURATION_MS) {
                countdownRunning = false
                remainingTimeMS = SPIELZEIT_DURATION_MS
            }
        } else {
            if (remainingTimeMS < 0 || remainingTimeMS > SPIELZEIT_DURATION_MS) {
                remainingTimeMS = SPIELZEIT_DURATION_MS
            }
        }
    }

    override fun reset() {
        remainingTimeMS = SPIELZEIT_DURATION_MS
    }

    private fun getTimeMs() = timeSource.getTimeMs()

}
