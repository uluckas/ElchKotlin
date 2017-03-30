package de.musoft.elch.presenter

import de.musoft.elch.alarm.AlarmTimer
import kotlinx.coroutines.experimental.Job
import java.util.concurrent.TimeUnit

/**
 * Created by ulu on 23/03/17.
 */

interface ElchView {

    fun setRemainingTime(value: Long, unit: TimeUnit)

}

class ElchPresenter(val alarmTimer: AlarmTimer) {

    var view = null as ElchView?
    var timerJob = null as Job?

    fun attachView(view: ElchView) {
        this.view = view
        alarmTimer.addSecondsListener(this::onSecondsChanged)
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
        view?.setRemainingTime(remainingSeconds, TimeUnit.SECONDS)
    }


}