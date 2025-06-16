package de.musoft.elch.kmp.app

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.russhwolf.settings.Settings // Will need actual settings passed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

// Expect factory for Settings (to be provided by each platform)
// internal expect fun createSettings(): Settings // This is now in SettingsFactory.kt

class TimerViewModel {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main) // Or Dispatchers.Default and switch for UI

    private val settings: Settings = createSettings() // Uses expect fun from SettingsFactory.kt

    private val alarmTimer = CommonAlarmTimer(
        settings = settings,
        coroutineScope = viewModelScope // Pass the scope
    )

    private val _timeDisplay = mutableStateOf("00:08:00") // Initial display
    val timeDisplay: State<String> = _timeDisplay

    private val _isTimerRunning = mutableStateOf(alarmTimer.isTimerRunning()) // Add isTimerRunning to CommonAlarmTimer
    val isTimerRunning: State<Boolean> = _isTimerRunning

    // Add a way for CommonAlarmTimer to tell us it's running or not
    private fun updateRunningState() {
        _isTimerRunning.value = alarmTimer.isTimerRunning()
    }

    init {
        alarmTimer.addSecondsListener { remainingSeconds ->
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            _timeDisplay.value = String.format("%02d:%02d:%02d", 0, minutes, seconds) // Assuming format 00:MM:SS
            updateRunningState()
        }
        // Initial update
        alarmTimer.triggerInitialUpdate() // Add this method to CommonAlarmTimer
    }

    fun onStartPauseClick() {
        alarmTimer.startOrPauseTimer()
        updateRunningState()
    }

    fun onResetClick() {
        alarmTimer.resetTimer()
        updateRunningState()
    }

    fun clear() {
        viewModelScope.cancel() // Cancel the scope when ViewModel is cleared
    }
}
