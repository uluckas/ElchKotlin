package de.musoft.elch.platform

import android.os.SystemClock


/**
 * Created by ulu on 31/03/17.
 */
class RealtimeClockSource : TimeSource {

    override fun getTimeMs(): Long = SystemClock.elapsedRealtime()

}