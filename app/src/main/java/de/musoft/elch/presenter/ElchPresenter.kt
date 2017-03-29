package de.musoft.elch.presenter

import de.musoft.elch.alarm.AlarmTimer
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit

/**
 * Created by ulu on 23/03/17.
 */

interface ElchView {

    fun setRemainingTime(time: Long, unit: TimeUnit)

}

class ElchPresenter(val alarmTimer: AlarmTimer) {

    var view = null as ElchView?
    var timerJob = null as Job?

    fun attachView(view: ElchView) {
        this.view = view
        view.setRemainingTime(alarmTimer.remainingTime, TimeUnit.MILLISECONDS)
    }

    fun detachView() {
        alarmTimer.removeSecondsListener(this::onSecondsChanged)
        this.view = null
    }

    fun startStopClicked() {
        alarmTimer.startOrPauseCountdown()
    }

    fun resetClicked() {
        alarmTimer.resetCountdown()
    }

    fun onSecondsChanged(remainingSeconds: Long) {
        view?.setRemainingTime(remainingSeconds)
    }


}