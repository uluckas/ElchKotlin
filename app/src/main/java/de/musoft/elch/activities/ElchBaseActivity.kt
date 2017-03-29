package de.musoft.elch.activities

import android.app.Activity
import android.os.Bundle
import de.musoft.elch.alarm.AlarmTimer
import de.musoft.elch.app.R
import de.musoft.elch.extensions.mToS
import de.musoft.elch.extensions.sToM
import kotlinx.android.synthetic.main.activity_elch.*

abstract class ElchBaseActivity : Activity(), AlarmTimer.SecondsListener {

    private val alarmTimer by lazy {
        AlarmTimer(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFabric()

        setContentView(R.layout.activity_elch)

        start_button.setOnClickListener { alarmTimer.startOrPauseTimer() }
        reset_button.setOnClickListener { alarmTimer.resetTimer() }
    }

    protected abstract fun setupFabric()

    override fun onResume() {
        super.onResume()
        alarmTimer.addSecondsListener(this)
    }

    override fun onPause() {
        alarmTimer.removeSecondsListener(this)
        super.onPause()
    }

    override fun onSecondsChanged(remainingSeconds: Long) {
        val minutes = remainingSeconds.sToM()
        val seconds = remainingSeconds - minutes.mToS()
        time.text = timeString(minutes, seconds)
    }

}

private fun timeString(minutes: Long, seconds: Long) = String.format("00:%02d:%02d", minutes, seconds)

