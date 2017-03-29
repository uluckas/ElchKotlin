package de.musoft.elch.activities

import android.app.Activity
import android.os.Bundle
import de.musoft.elch.alarm.AlarmTimer
import de.musoft.elch.app.R.layout.activity_elch
import de.musoft.elch.extensions.mToS
import de.musoft.elch.presenter.ElchPresenter
import de.musoft.elch.presenter.ElchView
import kotlinx.android.synthetic.main.activity_elch.*
import java.util.concurrent.TimeUnit

private fun timeString(minutes: Long, seconds: Long) = String.format("00:%02d:%02d", minutes, seconds)

abstract class ElchBaseActivity : Activity(), ElchView {

    private val presenter by lazy {
        ElchPresenter(AlarmTimer(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFabric()

        setContentView(activity_elch)

        start_button.setOnClickListener { presenter.startStopClicked() }
        reset_button.setOnClickListener { presenter.resetClicked() }
    }

    protected abstract fun setupFabric()

    override fun onResume() {
        super.onResume()
        presenter.attachView(this)
    }

    override fun onPause() {
        presenter.detachView()
        super.onPause()
    }

    override fun setRemainingTime(value: Long, unit: TimeUnit) {
        val minutes = unit.toMinutes(value)
        val seconds = unit.toSeconds(value) - minutes.mToS()
        timeLabel.text = timeString(minutes, seconds)
    }
}

