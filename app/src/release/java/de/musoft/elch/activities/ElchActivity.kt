package de.musoft.elch.activities

import android.app.Activity
import android.os.Bundle
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.beta.Beta
import de.musoft.elch.app.R
import de.musoft.elch.alarm.AlarmTimer
import de.musoft.elch.extensions.mToS
import de.musoft.elch.extensions.sToM
import io.fabric.sdk.android.DefaultLogger
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_elch.*

class ElchActivity : ElchBaseActivity(), AlarmTimer.SecondsListener {

    private fun setupFabric() {
        Fabric.with(Fabric.Builder(this)
                .kits(Crashlytics(), Beta())
                .build())
    }

}
