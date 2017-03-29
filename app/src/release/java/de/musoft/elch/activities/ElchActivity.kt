package de.musoft.elch.activities

import com.crashlytics.android.Crashlytics
import com.crashlytics.android.beta.Beta
import io.fabric.sdk.android.Fabric

class ElchActivity : ElchBaseActivity() {

    protected override fun setupFabric() {
        Fabric.with(Fabric.Builder(this)
                .kits(Crashlytics(), Beta())
                .build())
    }

}
