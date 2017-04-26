package de.musoft.elch.platform

import android.content.Context
import de.musoft.elch.delegates.KeyValueStore

/**
 * Created by ulu on 31/03/17.
 */

abstract class SharedPreferencesKeyValueStoreBase<V>(context: Context, name: String) :
        KeyValueStore<String, V> {

    protected val sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

}
