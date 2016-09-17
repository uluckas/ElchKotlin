package de.musoft.elch.activities

import android.app.Activity
import android.os.Bundle
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.beta.Beta
import com.example.elch.app.R
import de.musoft.elch.alarm.AlarmTimer
import de.musoft.elch.extensions.mToS
import de.musoft.elch.extensions.sToM
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_elch.*

private fun timeString(minutes: Long, seconds: Long) = String.format("00:%02d:%02d", minutes, seconds)

class ElchActivity : Activity() {

    private val alarmTimer by lazy {
        AlarmTimer(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics(), Beta())

        setContentView(R.layout.activity_elch)

        start_button.setOnClickListener { alarmTimer.startOrPauseCountdown() }
        reset_button.setOnClickListener { alarmTimer.resetCountdown() }
    }

    override fun onResume() {
        super.onResume()
        alarmTimer.addSecondsListener(onSecondsChanged)
    }

    override fun onPause() {
        alarmTimer.removeSecondsListener(onSecondsChanged)
        super.onPause()
    }

    val onSecondsChanged = { remainingSeconds: Long ->
        val minutes = remainingSeconds.sToM()
        val seconds = remainingSeconds - minutes.mToS()
        time.text = timeString(minutes, seconds)
    }

}

