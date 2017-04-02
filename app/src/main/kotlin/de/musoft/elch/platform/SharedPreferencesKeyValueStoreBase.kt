package de.musoft.elch.platform

import android.content.Context
import de.musoft.elch.delegates.KeyValueStore

/**
 * Created by ulu on 31/03/17.
 */

abstract class SharedPreferencesKeyValueStoreBase<V>(context: Context) :
        KeyValueStore<String, V> {

    protected val sharedPreferences = context.getSharedPreferences("TimerState", Context.MODE_PRIVATE)

}
