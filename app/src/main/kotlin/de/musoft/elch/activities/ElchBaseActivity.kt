package de.musoft.elch.activities

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.os.Bundle
import de.musoft.elch.alarm.AlarmScheduler
import de.musoft.elch.alarm.AlarmTimer
import de.musoft.elch.alarm.AlarmTimerModel
import de.musoft.elch.app.R.layout.activity_elch
import de.musoft.elch.extensions.alarmManager
import de.musoft.elch.extensions.mToS
import de.musoft.elch.platform.BooleanSharedPreferencesKeyValueStore
import de.musoft.elch.platform.LongSharedPreferencesKeyValueStore
import de.musoft.elch.platform.RealtimeClockSource
import de.musoft.elch.presenter.ElchPresenter
import de.musoft.elch.presenter.ElchView
import kotlinx.android.synthetic.main.activity_elch.*
import kotlinx.coroutines.experimental.android.UI
import java.util.concurrent.TimeUnit

private fun timeString(minutes: Long, seconds: Long) = String.format("00:%02d:%02d", minutes, seconds)

abstract class ElchBaseActivity : Activity(), ElchView {

    class Router(applicationContext: Context, private val view : ElchView) {
        private val model = AlarmTimerModel(BooleanSharedPreferencesKeyValueStore(applicationContext),
                LongSharedPreferencesKeyValueStore(applicationContext),
                RealtimeClockSource())
        private val alarmScheduler = AlarmScheduler(applicationContext)
        private val alarmTimer = AlarmTimer(model, alarmScheduler, UI)

        fun showElch() {
            view.presenter = ElchPresenter(alarmTimer)
        }
    }

    override var presenter = null as ElchPresenter?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFabric()

        setContentView(activity_elch)

        start_button.setOnClickListener { presenter?.startStopClicked() }
        reset_button.setOnClickListener { presenter?.resetClicked() }

        val view = this as ElchView
        val router = Router(applicationContext, view)
        router.showElch()
    }

    protected abstract fun setupFabric()

    override fun onResume() {
        super.onResume()
        presenter?.attachView(this)
    }

    override fun onPause() {
        presenter?.detachView()
        super.onPause()
    }

    /* ElchView */
    override fun setRemainingSeconds(value: Long) {
        val minutes = TimeUnit.SECONDS.toMinutes(value)
        val seconds = TimeUnit.SECONDS.toSeconds(value) - minutes.mToS()
        timeLabel.text = timeString(minutes, seconds)
    }
}


