package de.musoft.elch.activities

import android.app.Activity
import android.os.Bundle
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.beta.Beta
import com.example.elch.app.R.layout.activity_elch
import de.musoft.elch.alarm.AlarmTimer
import de.musoft.elch.extensions.mToS
import de.musoft.elch.presenter.ElchPresenter
import de.musoft.elch.presenter.ElchView
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_elch.*
import java.util.concurrent.TimeUnit

private fun timeString(minutes: Long, seconds: Long) = String.format("00:%02d:%02d", minutes, seconds)

class ElchActivity : Activity(), ElchView {

    private val presenter by lazy {
        ElchPresenter(AlarmTimer(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics(), Beta())

        setContentView(activity_elch)

        start_button.setOnClickListener { presenter.startStopClicked() }
        reset_button.setOnClickListener { presenter.resetClicked() }
    }

    override fun onResume() {
        super.onResume()
        presenter.attachView(this)
    }

    override fun onPause() {
        presenter.detachView()
        super.onPause()
    }

    override fun setRemainingTime(newTime: Long, unit: TimeUnit) {
        val minutes = unit.toMinutes(newTime)
        val seconds = unit.toSeconds(newTime) - minutes.mToS()
        timeLabel.text = timeString(minutes, seconds)
    }
}

