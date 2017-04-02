package de.musoft.elch.platform

import android.content.Context
import android.util.Log

/**
 * Created by ulu on 31/03/17.
 */

class BooleanSharedPreferencesKeyValueStore(context: Context) :
        SharedPreferencesKeyValueStoreBase<Boolean>(context) {

    override fun read(key: String, default: Boolean): Boolean {
        val value = sharedPreferences.getBoolean(key, default)
        Log.d("Prefs", "read '$key' ($default) -> '$value'")
        return value
    }
    override fun write(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
        Log.d("Prefs", "write '$key', '$value'")
    }
}
