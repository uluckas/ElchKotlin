package de.musoft.elch.activities

import com.crashlytics.android.Crashlytics
import com.crashlytics.android.beta.Beta
import de.musoft.elch.alarm.AlarmTimer
import io.fabric.sdk.android.DefaultLogger
import io.fabric.sdk.android.Fabric

class ElchActivity : ElchBaseActivity(), AlarmTimer.SecondsListener {

    private fun setupFabric() {
        Fabric.with(Fabric.Builder(this)
                .kits(Crashlytics(), Beta())
                .logger(DefaultLogger(1))
                .build())
    }
}

