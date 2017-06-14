package de.musoft.elch.presenter

import de.musoft.elch.alarm.AlarmTimer

/**
 * Created by ulu on 23/03/17.
 */

interface ElchView {

    var presenter: ElchPresenter?

    fun setRemainingSeconds(value: Long)

}

class ElchPresenter(val alarmTimer: AlarmTimer) {

    var view = null as ElchView?

    fun attachView(view: ElchView) {
        this.view = view
        alarmTimer.addSecondsListener(this::onSecondsChanged)
        view.setRemainingSeconds(alarmTimer.remainingTimeS)
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

    fun onSecondsChanged() {
        view?.setRemainingSeconds(alarmTimer.remainingTimeS)
    }


}